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
 * Description ʹ��Java��ɶ�һƪ�ı�����Ȼ������з֣��ڴ˸����������ı���ʽ��Ϊʾ��������һ�ָ�ʽ���ó����������ȷ�з֡�
 * </p>
 * 
 * @author TKPad wangx89@126.com
 *         <p>
 *         Date 2015��2��11�� ����1:33:03
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
		
		
		
		
		
        ArrayList<String> res = new ArrayList<String>();// �����зֽ��
        StringBuilder sb = new StringBuilder();// ƴ�Ӷ�ȡ������
        String temp = null;// ��ʱ�������洢sbȥ���ո������
        int ch = 0;
        while ((ch = reader.read()) != -1) {
            temp = sb.toString().trim().replaceAll("\\s*", "");// ȡ��ǰ��ո�֮��ȥ���м�ո�
            if ((char) ch == '\r') {
                // �ж��Ƿ��ǿ���
                if (!"".equals(temp)) {
                    // ˵�����˶����β������������������sb
                    res.add(temp);
                }
                sb.delete(0, sb.length());
            } else {
                // ˵��û�������β��������ݴ�
                sb.append((char) ch);
            }
        }
        if (reader.read() == -1) {
            System.err.println("������ļ�"+filepath+"��ȡ,���濪ʼ������");
        }
        // ���һ������ǿգ� �����һ�μ��룬���򲻴���
        if (!"".equals(temp)) {
            res.add(temp);
        }
  
        
        List<String> List = new ArrayList<String>();
        if(flag)                //��ͷҪ���ϱ�Ǳ����������Ϊ��һ������
        	List.add("PPPPP");
        else
        	List.add("TTTTT");
        //��ʼѭ���طֶ�ƥ��
        int i = 0;
        Iterator<String> iterator = res.iterator();//������
        while (iterator.hasNext()) 
        {

        	String next = iterator.next();
        	String[] strings = GreedyStringTiling.Split(next);//���÷ִ�ϵͳ����String����
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


		
//        System.err.println("����"+filepath+"��������");
//        System.out.println("����ĸ����ǣ�" + res.size());
    }
	
	
	
	
	
	
	
	
	
	
	
	//System.out.println();
/*	System.out.print("����λ�����Ϊ"+tiles.patternPostion+","+"�յ�Ϊ"+(tiles.patternPostion+tiles.length));
	System.out.print("("+tiles.patternPostion+",");
	System.out.print(tiles.textPosition+",");
	System.out.print(tiles.length+")");
	System.out.println();
	System.out.println();
	System.out.print("tile.length��");
	System.out.print(tiles.length);
	System.out.println();*/
}


//int Tmoved = 0;
//Tmoved += PList.length;
//	System.out.println("\nSuspected Plagirism: "+result.suspectedPlagiarism);
//	System.out.print("��ƪ�������ƴ��ﳤ���ܼ�Ϊ��"+copiedlength);
//	System.out.println("��ƪ�������ƴ��ﳤ���ܼ�Ϊ��"+copiedlength);


//82
//		System.out.println("Identifiers: "+result.getIdentifier().id1+":"+result.getIdentifier().id2);
//System.out.println("Similarity: "+result.getSimilarity());          //�������simility
//System.out.print("Plagiriasm tiles: ");







//126











/*
int copiedlength1 = 0;//1�������
//System.out.println("�ڸö��У����Ƴ�Ϯ���ַ���Ϊ��");
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



		System.err.println("�ı�1��"+para+"�����г�Ϯ���ɵľ���Ϊ��");
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
			System.out.println("�öι���"+PList.length+"������");
			System.out.println("���Ƴ�Ϯ����ռ�öεı���Ϊ��"+ bizhi1 +"%");
			System.err.println("�ָ���");
				continue;
			}
			//continue;
		}	
			
			
			
			
	//		System.out.print(copiedlength);
		}
	
*/