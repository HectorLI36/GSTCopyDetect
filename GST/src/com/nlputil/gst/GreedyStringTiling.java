package com.nlputil.gst;

import java.awt.font.NumericShaper.Range;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedMap;

import org.xjtu.Template.TemplateItem;

import com.nlputil.gst.NLPIRUtil.NLPIRInterface.result_t;

public class GreedyStringTiling {
	public static ArrayList<MatchVals> tiles = new ArrayList<MatchVals>();
	public static ArrayList<Queue<MatchVals>> matchList = new ArrayList<Queue<MatchVals>>();

	/**
	 * This method runs a comparison on the two given strings s1 and s2
	 * returning a PlagResult object containing the similarity value, the
	 * similarities as list of tiles and a boolean value indicating suspected
	 * plagiarism.
	 * 
	 * Input: s1 and s2 : normalized Strings mML : minimumMatchingLength
	 * threshold : a single value between 0 and 1 Output: PlagResult
	 * 
	 * @param s1
	 * @param s2
	 * @param mML
	 * @param threshold
	 * @return 
	 * @throws IOException 
	 */
	public static PlagResult run(String s1, String s2, int mML, float threshold,String filePath1,String filePath2) throws IOException {
		if (mML < 1)
			System.err
			.println("OutOfRangeError: minimum Matching Length mML needs to be greater than 0");
		if (!((0 <= threshold) && (threshold <= 1)))
			System.err
			.println("OutOfRangeError: treshold t needs to be 0<=t<=1");
		if (s1.isEmpty() || s2.isEmpty())
			System.err
			.println("NoValidArgumentError: input must be of type string not None");
		if (s1.equals("") || s2.equals(""))
			System.err
			.println("NoValidArgumentError: input must be of type string not None");

		//String[] PList = GetParagraph.DoneParaing(filePath1,true);
	//	String[] TList = GetParagraph.DoneParaing(filePath2,false);
		String[] PList = Split(s1);
		String[] TList = Split(s2);
//		String[] PListtemp = PList;
	//	String[] TListtemp = TList;
		
		
		
		
		
		tiles = RKR_GST(PList,TList,mML,5);
		
		
		// Compute Similarity
		SimVal simResult = SimilarityCalculator.calcSimilarity(			
				Arrays.asList(PList), Arrays.asList(TList),
				tiles, threshold);
		float similarity = simResult.similarity;
		if (similarity > 1)
			similarity = 1;

		// Create Plagiarism result and set attributes
		PlagResult result = new PlagResult(0, 0);
		result.setIdentifier(createKRHashValue(s1), createKRHashValue(s2));
		result.setTiles(tiles);
		result.setSimilarity(similarity);
		result.setSuspectedPlagiarism(simResult.suspPlag);

		
//		PList = Split(s1);
//		TList = Split(s2);	
	//	PList = GetParagraph.DoneParaing(filePath1,true);
	//	TList = GetParagraph.DoneParaing(filePath2,false);
		PList = Split(s1);
		TList = Split(s2);
		int m = 0;
		int i = 0;
		int para = 1;
		int j = 0;
		int copiedlength1 = 0;
		int loc = 0;
//		ArrayList<int> boundary = new ArrayList<int>();
		

		//List<String> bdr = new ArrayList<String>();
		int[] bdr = new int[9999];
		bdr[0] = 0;
		for(i=0;i<9999;i++)
		{
			for(j=i+1+loc;j<PList.length;j++)
			{if(PList[j]=="PPPPP")
				{
				loc = j;
				bdr[i+1] = j;
			    break;
			    }
			}
		}//找边界
		
		i=1;
/*		while(bdr[i]!=0)
		{copiedlength1 = 0;	
	//	int once = 0;
			System.out.println("文本一第"+i+"段中有抄袭嫌疑的句子为：");*/
			for(MatchVals tiles:result.getTiles()) //really matters
			{   
//				if((tiles.patternPostion>=bdr[i-1])&&((tiles.patternPostion+tiles.length)<=bdr[i]))
//					{
					
					     j = 0;
					
				for(m=(tiles.patternPostion);m<(tiles.length+tiles.patternPostion);m++ )
					{
//						if(PList[m]!="*"){
						System.out.print(PList[m]);
//						PList[m] = "*";
			            j++;
						}
//						//else
//						//	break;
//						}
//					if(j!=0)
				System.out.println();
	//				once = tiles.patternPostion;
					copiedlength1 += j;
				//	break;
				
			        }
				
 //             }
	//		System.out.println();
//			float bizhi1 = 100* (float)copiedlength1/(bdr[i]-bdr[i-1]-1);
			float bizhi1 = 100* (float)copiedlength1/PList.length;
//			System.out.println("疑似抄袭部分占该段的比重为："+ bizhi1 +"%");
			System.out.println("疑似抄袭部分占全文的比重为："+ bizhi1 +"%");
//			i++;
//			para++;
//			System.out.println();
//		}
		
		return result;
	}

	/**
	 * Computes Running-Karp-Rabin-Greedy-String-Tiling.
	 * 
	 * P pattern string T text string
	 * 
	 * More Informations can be found here: "String Similarity via Greedy String
	 * Tiling and Running Karp-Rabin Matching"
	 * http://www.pam1.bcs.uwa.edu.au/~michaelw/ftp/doc/RKR_GST.ps "YAP3:
	 * Improved Detection of Similarities in Computer Program and other Texts"
	 * http://www.pam1.bcs.uwa.edu.au/~michaelw/ftp/doc/yap3.ps
	 * 
	 * @author arunjayapal
	 * @param Pattern
	 *            string
	 * @param Text
	 *            String
	 * @param minimal
	 *            Matching length value
	 * @param Initialize
	 *            search size
	 * @return tiles
	 */
	public static ArrayList<MatchVals> RKR_GST(String[] PList, String[] TList,
			int minimalMatchingLength, int initsearchSize) {
		/*if (minimalMatchingLength < 1)
			minimalMatchingLength = 2;   //最小匹配
*/
		if (initsearchSize < 5)
			initsearchSize = 20;

		int s = 0;

		s = initsearchSize;
		boolean stop = false;
		while (!stop) {
			// Lmax is size of largest maximal-matches from this scan
			int Lmax = scanpattern(s, PList, TList);
			// if very long string no tiles marked. Iterate with larger s
			if (Lmax > 2 * s)
				s = Lmax;
			else {
				markStrings(s, PList, TList);
				if (s > (2 * minimalMatchingLength))
					s = s/2;
				else if (s > minimalMatchingLength)
					s = minimalMatchingLength;
				else
					stop = true;
			}
		}						
		return tiles;
	}

	/**
	 * Scans the pattern and text string lists for matches.
	 * 
	 * If a match is found that is twice as big as the search length s that size
	 * is returned, to be used to restart the scanpattern with it. All matches
	 * found are stored in a list of matches in queues.
	 * 
	 * @param s
	 * @param P
	 * @param T
	 * @return Longest maximum match
	 */
	//这里把hash构建好了，主要是t的
	public static int scanpattern(int s, String[] P, String[] T) {

		int longestMaxMatch = 0;
		Queue<MatchVals> queue = new LinkedList<MatchVals>();
		GSTHashTable hashtable = new GSTHashTable();
		/**
		 * Starting at the first unmarked token in T for each unmarked Tt do if
		 * distance to next tile <= s then advance t to first unmarked token
		 * after next tile else create the KR-hash value for substring Tt to
		 * Tt+s-1 and add to hashtable
		 */
		int t = 0;
		boolean noNextTile = false;
		int h;
		while (t < T.length) {
			if (isMarked(T[t])) {//没有走这一步
				t = t+1;
				continue;
			}

			int dist;
			if(distToNextTile(t, T) instanceof Integer)//如果是一个整数
				dist = (int)distToNextTile(t, T);
			else{
				dist = 0;
				dist = T.length - t;
				noNextTile = true;
			}
			//int dist = distToNextTile(t, T);
			// No next tile found

			if (dist < s) {
				if (noNextTile)
					t = T.length;
				else {
					if(jumpToNextUnmarkedTokenAfterTile(t, T) instanceof Integer)
						t = (int)jumpToNextUnmarkedTokenAfterTile(t, T);
					else
						t = T.length;
				}
			} else {
				StringBuilder sb = new StringBuilder();

				for (int i = t; i <= t + s-1; i++)
					sb.append(T[i]);
				String substring = sb.toString();
				h = createKRHashValue(substring);
				hashtable.add(h, t);
				t = t+1;
			}
		}

		/**
		 * Starting at the first unmarked token of P for each unmarked Pp do if
		 * distance to next tile <= s then advance p to first unmarked token
		 * after next tile else create the KR hash-value for substring Pp to
		 * Pp+s-1 check hashtable for hash of KR hash-value for each hash-table
		 * entry with equal hashed KR hash-value do if for all j from 0 to s-1,
		 * Pp+ j = Tt+ j then k: = s while Pp+k = Tt+k AND unmarked(Pp+k) AND
		 * unmarked(Tt+k) do k := k + 1 if k > 2 *s then return(k) else record
		 * new maximal-match
		 */
		noNextTile = false;
		int p = 0;
		while (p < P.length) {
			if (isMarked(P[p])) {
				p = p + 1;
				continue;
			}

			int dist;

			if(distToNextTile(p, P) instanceof Integer){
				dist = (int)distToNextTile(p, P);
			}
			else{
				dist = 0;
				dist = P.length - p;
				noNextTile = true;
			}

			if (dist < s) {
				if (noNextTile)
					p = P.length;
				else {

					if(jumpToNextUnmarkedTokenAfterTile(p, P) instanceof Integer)
						p = (int)jumpToNextUnmarkedTokenAfterTile(p, P);
					else{
						p = 0;
						p = P.length;
					}
				}
			} else {
				StringBuilder sb = new StringBuilder();
				for (int i = p; i <= p + s-1; i++) {
					sb.append(P[i]);
				}
				String substring = sb.toString();
				h = createKRHashValue(substring);
				ArrayList<Integer> values = hashtable.get(h);
				if (values != null) {
					for (Integer val : values) {
						StringBuilder newsb = new StringBuilder();
						for (int i = val; i <= val + s-1; i++) {
							newsb.append(T[i]);
						}
						if (newsb.toString().equals(substring)) {
							t = val;
							int k = s;

							while (p + k < P.length && t + k < T.length
									&& P[p + k].equals(T[t + k])
									&& isUnmarked(P[p + k])
									&& isUnmarked(T[t + k]))
								k = k + 1;

							if (k > 2 * s)
								return k;
							else {
								if (longestMaxMatch < s)
									longestMaxMatch = s;
								MatchVals mv = new MatchVals(p, t, k);
								queue.add(mv);
							}
						}
					}
				}
				p += 1;
			}

		}
		if (!queue.isEmpty()){
			matchList.add(queue);
		}
		return longestMaxMatch;
	}

	private static void markStrings(int s, String[] P, String[] T) {
		for(Queue<MatchVals> queue:matchList){
			while (!queue.isEmpty()) {
				MatchVals match = queue.poll();
				if (!isOccluded(match, tiles)) {
					for (int j = 0; j < match.length; j++) {
						P[match.patternPostion + j] = markToken(P[match.patternPostion + j]);
						T[match.textPosition + j] = markToken(T[match.textPosition + j]);
					}
					tiles.add(match);
				}
			}
		}
		matchList = new ArrayList<Queue<MatchVals>>(); 
	}

	/**
	 * Creates a Karp-Rabin Hash Value for the given substring and returns it.
	 * 
	 * Based on: http://www-igm.univ-mlv.fr/~lecroq/string/node5.html
	 * 
	 * @param substring
	 * @return hash value for any given string
	 */

	private static int createKRHashValue(String substring) {
		int hashValue = 0;
		for (int i = 0; i < substring.length(); i++)
			hashValue = ((hashValue << 1) + (int) substring.charAt(i));
		return hashValue;
	}

	/**
	 * If string s is unmarked returns True otherwise False.
	 * 
	 * @param string
	 * @return true or false (i.e., whether marked or unmarked)
	 */
	private static boolean isUnmarked(String string) {
		if (string.length() > 0 && string.charAt(0) != '*')
			return true;
		else
			return false;
	}

	private static boolean isMarked(String string) {
		return (!isUnmarked(string));
	}

	private static String markToken(String string) {
		StringBuilder sb = new StringBuilder();
		sb.append("*");
		sb.append(string);
		return sb.toString();
	}

	/**
	 * Returns true if the match is already occluded by another match in the
	 * tiles list.
	 * 
	 * "Note that "not occluded" is taken to mean that none of the tokens Pp to
	 * Pp+maxmatch-1 and Tt to Tt+maxmatch-1 has been marked during the creation
	 * of an earlier tile. However, given that smaller tiles cannot be created
	 * before larger ones, it suffices that only the ends of each new putative
	 * tile be testet for occlusion, rather than the whole maxmimal match." [
	 * "String Similarity via Greedy String Tiling and Running Karp-Rabin Matching"
	 * http://www.pam1.bcs.uwa.edu.au/~michaelw/ftp/doc/RKR_GST.ps]
	 * 
	 * @param match
	 * @param tiles2
	 * @return true or false
	 */
	private static boolean isOccluded(MatchVals match, ArrayList<MatchVals> tiles) {
		if(tiles.equals(null) || tiles == null || tiles.size() == 0)
			return false;
		for (MatchVals matches : tiles) {
			if ((matches.patternPostion + matches.length == match.patternPostion
					+ match.length)
					&& (matches.textPosition + matches.length == match.textPosition
					+ match.length))
				return true;
		}
		return false;
	}

	/**
	 * Returns distance to next tile, i.e. to next marked token. If not tile was
	 * found, it returns None.
	 * 
	 * case 1: there is a next tile -> pos + dist = first marked token -> return
	 * dist case 2: there is no next tile -> pos + dist = len(stringList) ->
	 * return None dist is also number of unmarked token 'til next tile
	 * 
	 * @param p
	 * @param p2
	 * @return distance to next tile
	 */
	private static Object distToNextTile(int pos, String[] stringList) {
		if (pos == stringList.length)
			return null;
		int dist = 0;
		while (pos+dist+1<stringList.length && isUnmarked(stringList[pos+dist+1]))
			dist += 1;
		if (pos+dist+1 == stringList.length) 
			return null;
		return dist+1;
	}

	/**
	 * Returns the first postion of an unmarked token after the next tile.

        case 1: -> normal case
            -> tile exists
            -> there is an unmarked token after the tile
        case 2:
            -> tile exists
            -> but NO unmarked token after the tile
        case 3:
            -> NO tile exists
	 * @param pos
	 * @param stringList
	 * @return the position to jump to the next unmarked token after tile
	 */
	private static Object jumpToNextUnmarkedTokenAfterTile(int pos, String[] stringList) {
		Object dist = distToNextTile(pos, stringList);
		if(dist instanceof Integer)
			pos = pos+ (int)dist;
		else
			return null;
		while (pos+1<stringList.length && (isMarked(stringList[pos+1])))
			pos = pos+1;
		if (pos+1> stringList.length-1) 
			return null;
		return pos+1;
	}

	//读取txt文件
	 public static String readTxtFile(String filePath){
		 String s = null;
		 StringBuilder sb = new StringBuilder(1200);
	        try {
	               String encoding="UTF-8";
	                File file=new File(filePath);
	                if(file.isFile() && file.exists()){ //判断文件是否存在
	                    InputStreamReader read = new InputStreamReader(
	                    new FileInputStream(file),encoding);//考虑到编码格式
	                    BufferedReader bufferedReader = new BufferedReader(read);
	                    //String lineTxt = null;

	                    while((s = bufferedReader.readLine()) != null){
	                    	sb.append(s);

	                      //  System.out.println(lineTxt);
	                 //  System.out.print(s);
	                    }
	                  read.close();
	        }else{
	            System.out.println("找不到指定的文件");
	        }
	        } catch (Exception e) {
	            System.out.println("读取文件内容出错");
	            e.printStackTrace();
	        }
	        return sb.toString();
	    }
	
	public static String[] Split(String s)
{
		    List<String> Tlist = new ArrayList<String>();
			result_t[] Twords = NLPIRUtil.NLPIR_ParagraphProcessA(s);
			int Tcharoff = 0, Tcoff, Tbl;
			for (int i=0; i<Twords.length; ++i) {
				for (Tcoff = Tcharoff, Tbl = 0; Tbl < Twords[i].length ; ++Tcoff) {
					char c = s.charAt(Tcoff);
					if (c >=0xff) Tbl += 3;
					else ++Tbl;
				}
				String wd = s.substring(Tcharoff, Tcoff);
				Tcharoff = Tcoff;
				Tlist.add(wd);
			}
			int Tsize=Tlist.size();  
			String[] TList = (String[])Tlist.toArray(new String[Tsize]);  
	return TList;
}
	/*
	public static void haveatry(String List) throws IOException
	{
//			NLPIRUtil.NLPIR_Exit();
 * //		NLPIRUtil.NLPIR_Init();
		try {
			Template temp = new Template(List);
	        SortedMap<TemplateItem, com.nlputil.gst.DocMatcher.Range> map = DocMatcher.structureMatch(temp, List);
			if (map == null) {
				System.err.println("Error run with file " + List.substring(0,3));
			}
			System.out.println("\r\n");
			
			System.out.println("\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

	
	public static void main(String s[]) throws IOException{
		String filePath1 = "C:\\Users\\lihao\\Desktop\\xiangsidu\\rpt-计算机21-2120505009-高鑫.txt";
		String filePath2 = "C:\\Users\\lihao\\Desktop\\xiangsidu\\rpt-计算机21-2120505013-李家诚.txt";

		String s1 = new String();
		String s2 = new String();
		s1 = readTxtFile(filePath1);
		s1 = s1.replaceAll(" ","");
        s2 = readTxtFile(filePath2);
        s2 = s2.replaceAll(" ", "");
        NLPIRUtil.NLPIR_Init();
	//	GetParagraph.DoneParaing(filePath1);
	//	GetParagraph.DoneParaing(filePath2);
		
        
    /*    try {
			haveatry(s1);
		} catch (IOException e) {

			e.printStackTrace();
		}
        
 //       DocMatcher.main(s1);
  //      String[] PList = Split(s1);
   //     haveatry(PList);*/
	   run(s1,s2,2,(float)0.4,filePath1,filePath2);	
	   NLPIRUtil.NLPIR_Exit();
	}
}

