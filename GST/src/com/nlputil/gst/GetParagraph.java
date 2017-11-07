package com.nlputil.gst;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * <p>
 * ClassName GetParagraph
 * </p>
 * <p>
 * Description 使用Java完成对一篇文本的自然段落的切分，在此给出了五种文本格式作为示例，对任一种格式，该程序均可以正确切分。
 * </p>
 * 
 * @author TKPad wangx89@126.com
 *         <p>
 *         Date 2015年2月11日 下午1:33:03
 *         </p>
 * @version V1.0.0
 *
 */
public class GetParagraph {
	
//public static void main(String[] args) throws IOException {
	public static String[] DoneParaing (String filepath,boolean flag ) throws IOException {
		
		
		
		
		
		InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(filepath)), "UTF-8");  
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(isr);  
		
		
		
		
		
        ArrayList<String> res = new ArrayList<String>();// 段落切分结果
        StringBuilder sb = new StringBuilder();// 拼接读取的内容
        String temp = null;// 临时变量，存储sb去除空格的内容
        int ch = 0;
        while ((ch = reader.read()) != -1) {
            temp = sb.toString().trim().replaceAll("\\s*", "");// 取出前后空格，之后去除中间空格
            if ((char) ch == '\r') {
                // 判断是否是空行
                if (!"".equals(temp)) {
                    // 说明到了段落结尾，将其加入链表，并清空sb
                    res.add(temp);
                }
                sb.delete(0, sb.length());
            } else {
                // 说明没到段落结尾，将结果暂存
                sb.append((char) ch);
            }
        }
        if (reader.read() == -1) {
            System.err.println("已完成文件"+filepath+"读取,下面开始分析：");
        }
        // 最后一段如果非空， 将最后一段加入，否则不处理
        if (!"".equals(temp)) {
            res.add(temp);
        }
  
        
        List<String> List = new ArrayList<String>();
        if(flag)                //开头要做上标记表明其后方内容为第一段内容
        	List.add("PPPPP");
        else
        	List.add("TTTTT");
        //开始循环地分段匹配
        int i = 0;
        Iterator<String> iterator = res.iterator();//迭代器
        while (iterator.hasNext()) 
        {

        	String next = iterator.next();
        	String[] strings = GreedyStringTiling.Split(next);//调用分词系统存入String数组
        	for(i=0;i<strings.length;i++)
        	{
        		List.add(strings[i]);
        	}
        	if(flag)
        	    List.add("PPPPP");
        	else
        		List.add("TTTTT");

        }
		int size=List.size();  
		String[] RList = (String[])List.toArray(new String[size]);
		flag = false;
		
		return RList;


		
//        System.err.println("文章"+filepath+"分析如下");
//        System.out.println("段落的个数是：" + res.size());
    }
	
	
	
	
	
	
	
	
	
	
	
	//System.out.println();
/*	System.out.print("相似位置起点为"+tiles.patternPostion+","+"终点为"+(tiles.patternPostion+tiles.length));
	System.out.print("("+tiles.patternPostion+",");
	System.out.print(tiles.textPosition+",");
	System.out.print(tiles.length+")");
	System.out.println();
	System.out.println();
	System.out.print("tile.length是");
	System.out.print(tiles.length);
	System.out.println();*/
}


//int Tmoved = 0;
//Tmoved += PList.length;
//	System.out.println("\nSuspected Plagirism: "+result.suspectedPlagiarism);
//	System.out.print("两篇文章相似词语长度总计为："+copiedlength);
//	System.out.println("两篇文章相似词语长度总计为："+copiedlength);


//82
//		System.out.println("Identifiers: "+result.getIdentifier().id1+":"+result.getIdentifier().id2);
//System.out.println("Similarity: "+result.getSimilarity());          //这里输出simility
//System.out.print("Plagiriasm tiles: ");







//126











/*
int copiedlength1 = 0;//1用于输出
//System.out.println("在该段中，疑似抄袭的字符串为：");
int i = 0;
int para = 0;
boolean flag = true;
//int j = 0;
int boundary = 0;
/*		for(i=0;i<PList.length;i++)
{
	if(PList[i]=="PPPPP")
	{
		para++;
		int k = ++i;
		for(;k<PList.length;k++)
		{if(PList[k]=="PPPPP")
			boundary = k;
		    continue;
		}



		System.err.println("文本1第"+para+"段中有抄袭嫌疑的句子为：");
		for(MatchVals tiles:result.getTiles()) //really matters
	{flag = true;
			//while(flag)
		{
			for(;i<PList.length;i++)
			{
				if(PList[i]=="PPPPP")
				{
					para++;
					int k = ++i;
					for(;k<PList.length;k++)
					{if(PList[k]=="PPPPP")
						{boundary = k;
					     continue;
					    }
					}
				}
				int m = 0;
			int j = 0;
		    for(m=(tiles.patternPostion);m<(tiles.length+tiles.patternPostion);m++ )
			{if(m>i&&m<boundary)
			 System.out.print(PList[m]);
			j++;
		//	flag = false;
			}
			copiedlength1 += j;
			System.out.println();
			float bizhi1 = 100* (float)copiedlength1/PList.length;
			float bizhi2 = 100*(float)copiedlength1/TList.length;
//			System.out.println();
			System.out.println("该段共有"+PList.length+"个词语");
			System.out.println("疑似抄袭部分占该段的比重为："+ bizhi1 +"%");
			System.err.println("分隔线");
				continue;
			}
			//continue;
		}	
			
			
			
			
	//		System.out.print(copiedlength);
		}
	
*/