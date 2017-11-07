package com.nlputil.gst;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.xjtu.SimilarityLD;
import org.xjtu.Template.ItemType;
import org.xjtu.Template.TemplateItem;


public class DocMatcher {
	public static class HeadingLine {
		public int lineno, headingno;
		public TemplateItem ti = null;
		public HeadingLine(short lno, short hno, TemplateItem it) {
			lineno = lno;
			headingno = hno;
			ti = it;
		}
	}
	
	public static class Range implements Comparable<Range> {
		public int start;
		public int end;
		public Range(int s, int e) {
			start = s;
			end = e;
		}
		
		public boolean equals(Object r) {
			if (this == r) {
			    return true;
			}
			if (r instanceof Range) {
			    Range rg = (Range)r;
			    return rg.start == start && rg.end == end;
			}
			return false;
		}
		
		public int hashCode() {
			return start + end * 256;
		}

	    @Override
	    public int compareTo(Range r) {
	        if (start != r.start)
	        	return start - r.start;
	        else
	        	return end - r.end;
	    }
	    
	    @Override
	    public String toString() {
	    	return "(" + start + ":" + end + ")";
	    }

	    public boolean contains(Range r) {
	    	return start <= r.start && end >= r.end;
	    }
	}

	private static HashMap<Integer, HeadingLine> hmap_headingline = null;

	private static int prefix(String line) {
		if (Template.isCardinal(line.charAt(0)) && Template.isDelim(line.charAt(1))) {
			for (int i = 2;i < line.length(); ++i) {
				if (!Character.isWhitespace(line.charAt(i)))
					return i;
			}
		}
		return 0;
	}

	private static double indexMatch(TemplateItem cur, String line) {
		int l = cur.item_text.length();
		line = line.replaceAll("[\\s\u00A0]*", "");
		if (cur.noedit) {
			char firstchar = cur.item_text.charAt(0);
			if (cur.item_text.equals(line) || line.startsWith(cur.item_text))
				return 1.0;
			else if (Character.isDigit(firstchar)) {
				int pf1 = prefix(line);
				int pf2 = prefix(cur.item_text);
				String lp = line.substring(pf1);
				String itp = cur.item_text.substring(pf2);
				if (lp.equals(itp) || lp.startsWith(itp))
					return 0.8;
				else
					return SimilarityLD.sim(lp, itp);
			}
			else {
				l = Math.min(l+2, line.length());
				return SimilarityLD.sim(cur.item_text, line.substring(0, l));
			}
		}
		else {
			String []toks = cur.item_text.split("XXX");
			int ll = line.length();
			if (ll < l * 4 && ll >= l && line.startsWith(toks[0])) {
				if (line.endsWith(toks[1]))
					return 0.8;
				else
					return SimilarityLD.sim(line.substring(ll - toks[1].length()), toks[1]);
			}
		}
		return 0.0;
	}
	
	private static int looseMatch(String p, String t) {
		int i;
		for (i = 0; i < p.length(); ++ i) {
			if (p.charAt(i) != t.charAt(i) && !
				(Template.isDelim(p.charAt(i)) && Template.isDelim(t.charAt(i))))
				return i;
		}
		return i;
	}

	private static double headingMatchBonus (int lno) {
		HeadingLine hl = hmap_headingline.get(lno);
		double bonus = 0.0;
		if (hl != null) {
			if (hl.ti == null)
				bonus += (hl.headingno == 2)?0.35:0.25;
			else
				bonus -= 0.3;
		}

		return bonus;
	}

	private static int slowIndex1Match(SortedMap<TemplateItem, Integer> mapTiLno, TemplateItem idx1, Vector<String> lines) {
		String line;
		int bestln = -1, lno, slno = 0, elno = lines.size();
		int ni = 0, got = -1;

		// 1. estimate its range
		for (TemplateItem ti: idx1.parent.children) {
			lno = mapTiLno.get(ti).intValue();
			if (ti.item_type == ItemType.TI_TYPE_INFO) {
				if (lno > slno)
					slno = lno;
			}
			else if (ti.item_type == ItemType.TI_TYPE_INDEX) {
				if (ti == idx1)
					got = ni;
				else if (got >= 0) {
					if (elno > lno && lno > 0)
						elno = lno;
				}
				else {
					if (lno > slno)
						slno = lno;
				}
				++ ni;
			}
		}
		
		// 2. do scanning ....
		assert(slno >= 0 && elno >= slno);
		double bestsr = 0.0;
		String p = idx1.item_text;
		for (lno = slno; lno < elno; ++ lno) {
			line = lines.elementAt(lno).trim();
			if (line.length() < 3)
				continue;
			if (line.charAt(0) != p.charAt(0)) {
				if (idx1.noedit && line.length() > p.length() * 2)
					continue;
				if (!idx1.noedit && line.length() > p.length() * 4)
					continue;
			}
			double sr;
			if (idx1.noedit) {
				sr = SimilarityLD.sim(p, line);
			}
			else {
				String []toks = p.split("XXX");
				int ll = toks[0].length();
				if (line.length() < ll)
					continue;
				sr = (double) looseMatch(toks[0], line) / (ll+1);
			}
			sr += headingMatchBonus(lno);
			if (sr > bestsr) {
				bestsr = sr;
				bestln = lno;
			}
		}
		
		return bestln;
	}

	private static void hmapPut(int bestln, TemplateItem ti, SortedMap<TemplateItem, Integer> mapTiLno) {
		mapTiLno.put(ti, new Integer(bestln));
		if (bestln >= 0) {
			HeadingLine hl = hmap_headingline.get(bestln);
			if (hl != null)
				hl.ti = ti;
		}
	}
	
	private static void tryMatchLeaves(TemplateItem idx2, Vector<String> lines, SortedMap<TemplateItem, Integer> mapTiLno) {
		int i, startln = mapTiLno.get(idx2).intValue(), endln;
		String buffered_line = null;
		boolean found = false;

		endln = idx2.children.size() + 2 + startln;

		for (TemplateItem leaf: idx2.children) {
			int len_ti = leaf.item_text.length();
			if (len_ti == 0) continue; // only <NewPage>
			// Avoid  java.lang.ArrayIndexOutOfBoundsException
			int eoln = Math.min(endln, lines.size()-1);
			for (i = 1; i<eoln-startln; ++i) {
				String line = lines.get(startln + i).trim();
				int len_ln = line.length();
				// skip null line, or hints with no '['
				if (len_ln == 0 || !leaf.noedit && line.charAt(0) != '[')
					continue;
				// exactly match
				else if (len_ln == len_ti && line.equals(leaf.item_text)) {
					found = true;
					break;
				}
				// TI text 1 2, line text 1 2 3 4; so '3 4' buffered for next match
				else if (line.startsWith(leaf.item_text)) {
					if (len_ln > len_ti + 2)
						buffered_line = line.substring(len_ti);
					found = true;
					break;
				}
				// buffered line matches
				else if (buffered_line != null && buffered_line.equals(leaf.item_text)) {
					found = true;
					buffered_line = null;
					break;
				}
				// TI text 1 2 3 4, line text 1 2; so '3 4' buffered for next match
				else if (leaf.item_text.startsWith(line)) {
					found = true;
					break;
				}
				// slow match for <NoEdit> leaves - LD similarity check
				else /*if (leaf.noedit)*/ {
					if (buffered_line != null) {
						line = buffered_line + line;
						len_ln += buffered_line.length();
						buffered_line = null;
					}
					if (len_ti >= len_ln && len_ti - len_ln < len_ti/2 || len_ti <= len_ln && len_ln - len_ti < len_ti/2) {
						double sr = SimilarityLD.sim(line, leaf.item_text);
						if (sr > 0.6) {
							found = true;
							break;
						}
					}
					else if (len_ti > len_ln) {
						buffered_line = line;
					}
				}
			}
			if (found) {
				mapTiLno.put(leaf, startln + i);
				startln = startln + i;
				found = false;
			}

		}
	}

	private static SortedMap<TemplateItem, Range> structMatch(Template temp, Vector<String> lines, Vector<Integer> offs, 
			HashMap<TemplateItem, Vector<Integer>> hmap) {
		SortedMap<TemplateItem, Integer> mapTiLno = new TreeMap<TemplateItem, Integer>(); // Map TI -> its line no 
		Vector<Integer> vi;
		String line;
		int bestln = -1, lnoint;

		// 1. match root - title
		TemplateItem root = temp.getRoot();
		vi = hmap.get(root);
		if (vi == null)
			bestln = -1; // TODO
		else {
			for (Integer lno: vi) {
				lnoint = lno.intValue();
				line = lines.elementAt(lnoint).replaceAll("\\s*", "");
				if (!line.equals(root.item_text)) {
					// Try to append the next line for title match
					line += lines.elementAt(1+lnoint).replaceAll("\\s*", "");
				}
				if (line.equals(root.item_text)) {
					bestln = lnoint;
				}
				else {
					if (SimilarityLD.sim(line, root.item_text) > 0.7)
						bestln = lnoint;
					else
						bestln = -1;
				}
				break; // only first matched line tested, others dropped
			}
			vi.clear();
		}
		assert(bestln < 15);
		hmapPut(bestln, root, mapTiLno);
		
		// 2. match infos & index1 /0,1,2... & appendix ...
		for (TemplateItem idx1: root.children) {
			bestln = -1;
			vi = hmap.get(idx1);
			if (vi != null) {
				for (Integer lno: vi) {
					lnoint = lno.intValue();
					line = lines.elementAt(lnoint).replaceAll("\\s*", "");
					
					// 2.1 match info
					if (idx1.item_type == ItemType.TI_TYPE_INFO) {
						if (line.length() > 32)
							continue;
						assert (lnoint < 32); // Infos assert in page 1!
						String idi = idx1.item_text.replaceAll("\\s*", "");
						String []toks = idi.split("锛�");
						idi = toks[0] + "锛�";
						if (line.startsWith(idi)) {
							bestln = lnoint;
						}
					}
					// 2.2 match index1/0,1 ...
					else if (!idx1.isappendix) {
						double sr = indexMatch(idx1, line) + headingMatchBonus(lnoint);
						if (sr > 0.7)
							bestln = lnoint;
					}
					// 2.3 match appendix ...
					else {
						if (line.equals(idx1.item_text))
							bestln = lnoint;
						else {
							double sr = indexMatch(idx1, line) + headingMatchBonus(lnoint);
							if (sr > 0.7)
								bestln = lnoint;
						}
					}
					if (bestln > 0)
						break;
				}
				vi.clear();
			}
			hmapPut(bestln, idx1, mapTiLno);
		}

		// 2.4 if the index and appendix are ordered/found?
		int lastlno = -1;
		for (TemplateItem idx1: root.children) {
			if (idx1.item_type != ItemType.TI_TYPE_INFO) {
				lnoint = mapTiLno.get(idx1).intValue();
				if (lnoint < 0 && !idx1.isappendix) {
					System.err.println(idx1.item_text + " - has not found!");
					lnoint = slowIndex1Match(mapTiLno, idx1, lines);
					if (lnoint < 0)
						return null;
					else
						hmapPut(lnoint, idx1, mapTiLno);
				}
				if (lnoint > 0 && lnoint < lastlno) {
					System.err.println("Index - "+ idx1.item_text + " - not order!");
					return null;
				}
				lastlno = lnoint;
			}
		}
	
		// 3. match index2 /0,1,2... 
		for (TemplateItem idx1: root.children) {
			if (idx1.item_type == ItemType.TI_TYPE_INFO)
				continue;
			else if (idx1.isappendix)
				break;
			int startln = mapTiLno.get(idx1).intValue();
			int lastbl = startln;
			for (TemplateItem idx2: idx1.children) {
				assert(idx2.index_level == 2 && idx2.item_type == ItemType.TI_TYPE_INDEX);
				vi = hmap.get(idx2);
				if (vi != null) {
					bestln = -1;
					for (Integer lno: vi) {
						lnoint = lno.intValue();
						if (lnoint < startln)
							continue;
						line = lines.elementAt(lnoint).replaceAll("\\s*", "");
						double sr = indexMatch(idx2, line) + headingMatchBonus(lnoint);
						if (sr > 0.75) {
							lastbl = bestln = lnoint;
							break;
						}
					}
				}
				if (bestln < 0) {
					int tl = idx2.item_text.length();
					for (int i = lastbl; i < lines.size(); ++i) {
						line = lines.elementAt(i).replaceAll("\\s*", "");
						if (line.length() < 3 || line.length() > tl * 2)
							continue;
						double sr = indexMatch(idx2, line) + headingMatchBonus(i);
						if (sr > 0.75) {
							bestln = i;
							break;
						}
					}
				
				}
				hmapPut(bestln, idx2, mapTiLno);
			}
		}

		// 4. sort by lineno and try to find unresolved TIs by LD similarity check
		List<Map.Entry<TemplateItem, Integer>> list =
	            new LinkedList<Map.Entry<TemplateItem, Integer>>(mapTiLno.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<TemplateItem, Integer>>()  {
	            public int compare(Map.Entry<TemplateItem, Integer> o1, Map.Entry<TemplateItem, Integer> o2) {
	                return (o1.getValue()).compareTo( o2.getValue() );
	            }
	        } );

	    Map<TemplateItem, Integer> sortedmap = new LinkedHashMap<TemplateItem, Integer>();
	    for (Map.Entry<TemplateItem, Integer> entry : list) {
	    	sortedmap.put( entry.getKey(), entry.getValue() );
	    }
	    
	    // 4.1 try to find unresolved TIs ...
	    Vector<Map.Entry<TemplateItem, Range>> vlr = new Vector<Map.Entry<TemplateItem, Range>>();
	    for (Map.Entry<TemplateItem, Integer> e: sortedmap.entrySet()) {
	    	lnoint = e.getValue().intValue();
	    	TemplateItem ti = e.getKey();
	    	if (lnoint < 0) {
	    		if (ti.item_type == ItemType.TI_TYPE_INDEX && ti.index_level == 2) {
	    			Integer idx1ln = mapTiLno.get(ti.parent);
	    			assert (idx1ln != null);
	    			DocMatcher.Range lr = new DocMatcher.Range (idx1ln.intValue(), -1);
	    			Map.Entry<TemplateItem, Range> lre = new AbstractMap.SimpleEntry<TemplateItem, Range>(ti, lr);
	    			vlr.add(lre);
	    		}
	    	}
	    	else if (ti.item_type == ItemType.TI_TYPE_INDEX && ti.index_level == 2) {
	    		for (Map.Entry<TemplateItem, Range> urtilr: vlr) {
	    			TemplateItem urti = urtilr.getKey();
	    			Range urlr = urtilr.getValue();
	    			if (ti.parent != urti.parent)
	    				continue;
	    			else if (ti.index_no == urti.index_no - 1) {
	    				 if (urlr.end > 0 && urlr.end < urlr.start)
	    					 urlr.end = lnoint;
	    				 else {
	    					 urlr.end = lnoint;
	    				 }
	    			}
	    			else if (ti.index_no == urti.index_no + 1) {
	    				if (urlr.end < 0) {
	    					urlr.end = urlr.start;
	    					urlr.start = lnoint;
	    				}
	    				else {
	    					urlr.start = urlr.end;
	    					urlr.end = lnoint;
	    				}
	    			}
	    		}
	    	}
	    	else if (ti.item_type == ItemType.TI_TYPE_INDEX && ti.index_level == 1) {
//	    		for (Map.Entry<TemplateItem, Range> urtilr: vlr) {
//	    			TemplateItem urti = urtilr.getKey();
//	    			Range urlr = urtilr.getValue();
//	    			if (urti.parent == ti || urlr.start )
//	    		}
	    	}
			//System.out.println(e.getKey().item_text + " = " + e.getValue().intValue());
		}
	    for (Map.Entry<TemplateItem, Range> urtilr: vlr) {
			TemplateItem urti = urtilr.getKey();
			Range urlr = urtilr.getValue();
			String itxt = urti.item_text;
			double maxsr = 0.0;
	System.err.println("{"+itxt.replaceAll("\\s*", "") + "}");
			if (urlr.end < 0)
				urlr.end = Math.min(lines.size(), urlr.start + 21) - 1;
			if (urlr.end < urlr.start) {
				int t = urlr.end;
				urlr.end = urlr.start;
				urlr.start = t;
			}
			for (int i = urlr.start + 1; i < urlr.end; ++i) {
				line = lines.elementAt(i).replaceAll("\\s*", "");
				if (line.length() < 2) continue;
				if (Character.isDigit(itxt.charAt(0))) {
					int pf1 = prefix(line);
					int pf2 = prefix(itxt);
					String itp = itxt.substring(pf2);
					String lp = line.substring(pf1);
					if (lp.length() > itp.length() + 2)
						lp = lp.substring(0, itp.length() + 2); // avoid too long text
					if (lp.equals(itp) || lp.startsWith(itp)) {
						bestln = i;
						break;
					}
					else {
						double sr = SimilarityLD.sim(lp, itp);
						sr += headingMatchBonus(i);
	if (sr > 0.33)	System.err.println("[" + lp + "]" + sr);
						if (sr > maxsr) {
							maxsr = sr;
							bestln = i;
						}
					}
				}
			}
			if (maxsr < 0.6) {
				bestln = -1;
			}
			//mapTiLno.put(urti, new Integer(bestln));
			hmapPut(bestln, urti, mapTiLno);
	    }

	    // 4.3 match other leaf items (fixed text or hints)
	    for (TemplateItem idx1: root.children) {
			if (idx1.item_type == ItemType.TI_TYPE_INFO)
				continue;
			else if (idx1.isappendix)
				break;
			for (TemplateItem idx2: idx1.children) {
				assert(idx2.index_level == 2 && idx2.item_type == ItemType.TI_TYPE_INDEX);
				tryMatchLeaves(idx2, lines, mapTiLno);
				
			}
	    }

		// 5. sort again by lineno and try to find unresolved TIs and show it
		list = new LinkedList<Map.Entry<TemplateItem, Integer>>(mapTiLno.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<TemplateItem, Integer>>() {
			public int compare(Map.Entry<TemplateItem, Integer> o1, Map.Entry<TemplateItem, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		sortedmap.clear();
		for (Map.Entry<TemplateItem, Integer> entry : list) {
			sortedmap.put(entry.getKey(), entry.getValue());
		}

		Map.Entry<TemplateItem, Integer> laste = null;
		int lastoff = 0;
		SortedMap<TemplateItem, Range> map = new TreeMap<TemplateItem, Range>();

		for (Map.Entry<TemplateItem, Integer> e : sortedmap.entrySet()) {
			TemplateItem ti = e.getKey(), lastti;
			int lno = e.getValue().intValue();
			int off = (lno >= 0)?offs.get(lno):-1;
			int tlen = (lno >= 0)?lines.get(lno).length():0;
			if (laste != null) {
				int llno = laste.getValue().intValue();
				if (llno >= 0 && llno < offs.size()) {
					int loff = offs.get(llno);
					int rlen = off - loff - tlen;
					lastti = laste.getKey();
					map.put(lastti, new Range(llno, lno-1));
//					System.out.println(lastti.item_text + " = " + llno + "[" + lastti.index_level +"/" + lastti.index_no + "/" 
//							+ lastti.copyratio + "/" + lastti.word_min + "-" + lastti.word_max + "/" + loff + "]" + "<" + rlen + ">");
				}
				laste = null;
			}
			if (true /*ti.copyratio >= 0.001*/) {
				laste = e;
				lastoff = off;
			}
			else {
//				System.out.println(ti.item_text + " = " + lno + "[" + ti.index_level +"/" + ti.index_no + "/" 
//						+ ti.copyratio + "/" + ti.word_min + "-" + ti.word_max + "/" + off + "]");
			}
		}

		return map;
	}

	/**
	 * If line matches <ti0, ti1, ...>, then put lno into hashmap with keys of ti0, ti1, ...
	 * @param hmap: hashmap of ti->V<lno>
	 * @param vti: vector of TemplateItem
	 * @param lno: line no
	 */
	private static void put(HashMap<TemplateItem, Vector<Integer>> hmap, Vector<TemplateItem> vti, int lno) {
		Integer lnoint = new Integer(lno);//取整？
		for (TemplateItem ti: vti) {
			Vector<Integer> vi = hmap.get(ti);
			if (vi == null)
				vi = new Vector<Integer>();
			vi.add(lnoint);
			hmap.put(ti, vi);
		}
	}

	private static SortedMap<TemplateItem, Range> structureMatch(Template temp, Vector<String> lines, Vector<Integer> offs) {
		/**
		 * map ti to its candidates line numbers.
		 */
		HashMap<TemplateItem, Vector<Integer>> hmap = new HashMap<TemplateItem, Vector<Integer>>();
		hmap_headingline = new HashMap<Integer, HeadingLine> (16);
		int lno = 0, hno = -1, idx = -1;
		String ln;
		for (String line: lines) {
			if (line.length() <= 3) {
				++ lno;
				continue;
			}
			hno = -1;
			if (line.charAt(0) == '<') {    //是在给ln赋值
				idx = line.indexOf('>'); //在定义
				String heading = line.substring(1, idx);
				ln = line.substring(idx+1);
				if (heading.startsWith("閺嶅洭顣�") ||heading.startsWith("heading ")) {
					// <heading X>
					hno = Integer.parseInt(heading.substring(heading.length()-1));
				}
				lines.set(lno, ln);
			}
			else
				ln = line;
			Vector<TemplateItem> vti = temp.getMatchedItems(ln);//对字符串的处理
			if (vti != null)
				put(hmap, vti, lno);
			if (hno > 0) {
				HeadingLine hl = new HeadingLine((short)lno, (short) hno, null);
				hmap_headingline.put(new Integer(lno), hl);
			}
			++ lno;
		}
		
	return structMatch(temp, lines, offs, hmap);
	}
	
	public static SortedMap<TemplateItem, Range> structureMatch(Template temp, String filename) throws IOException 
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
		Vector<String> lines = new Vector<String>(20);
		Vector<Integer> offs = new Vector<Integer>(20);
		String line = null;
		int off = 0;
		while ((line = br.readLine()) != null) {
			offs.add(off);
			off += line.length(); // '\r\n' ignored
			lines.add(line.trim());
		}
		br.close();

		return structureMatch(temp, lines, offs);
	}

	/**
	 * @param args
	 */
	
	
	
	   public static void main(String[] args)
	   {
		// TODO Auto-generated method stub
		String args1 = new String();
		args1 = "实验报告是体现学生实验完成质量的重要形式。但目前许多学生对实验报告不重视，不遵照格式写报告，乃至于抄袭报告。";
		if (args1.length() < 2)
			return;
		try {
			System.out.println("[File - " + args1.substring(0,3) + "]");
			System.err.println("[File - " + args1.substring(0,3) + "]");
			Template temp = new Template(args1);
			SortedMap<TemplateItem, Range> map = structureMatch(temp, args1);
			if (map == null) {
				System.err.println("Error run with file " + args1.substring(0,3));
			}
			System.out.println("\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
