package com.nlputil.gst;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lab report Template -
 *   Head - Info - Index1/0 - Index2/0 == text, Index2/1, ...
 *               - Index1/1 - Index2/0, Index2/1, ...
 * @author Administrator
 *
 */
public class Template {
	public static enum ItemType { TI_TYPE_INDEX, TI_TYPE_INFO, TI_TYPE_HINT, TI_TYPE_FIXED_TEXT, TI_TYPE_NEWPAGE, TI_TYPE_OTHER };

	public class TemplateItem implements Comparable<TemplateItem> {		
		public static final String Tag_Index0 = "Index0";
		public static final String Tag_Index1 = "Index1";
		public static final String Tag_Index2 = "Index2";
		public static final String Tag_IdInfo = "IdInfo";
		public static final String Tag_DateInfo = "DateInfo";
		public static final String Tag_NewPage = "NewPage";
		public static final String Tag_NoEdit = "NoEdit";
		public static final String Tag_CopyRatio = "CopyRatio";
		public static final String Tag_Mandated = "Mandated";
		//public static final String Tag_Hint = "";
		//public static final String Tag_Words = "字数：";
		//public static final String Tag_Pictures = "图：";
		//public static final String Tag_Pages = "页数：";
		public static final String Tag_Words_Pattern = "字数：([0-9]+)-([0-9]+)";
		public static final String Tag_Pictures_Pattern = "图：([0-9]+)\\+?|([0-9]+)-([0-9]+)个";
		public static final String Tag_Pages_Pattern = "页数：([0-9]+)\\+?|([0-9]+)-([0-9]+)页";
		public static final String Tag_Appendix_Pattern = "^附录.+";

		public ItemType item_type = ItemType.TI_TYPE_OTHER;
		public String item_text = null;
		public List<TemplateItem> children = null;
		public TemplateItem parent = null;

		public int index_level = 100;
		public int index_no = 0;
		public int heading_no = 0;
		private int tino;

		public boolean noedit = false;
		public boolean head = false;
		public boolean mandated = false;
		public boolean isappendix = false;
	
		public double copyratio = 0.0;
		public int word_min = 0;
		public int word_max = 0;
		public int pics_min = 0;
		public int pics_max = 0;
		public int pages_min = 0;
		public int pages_max = 0;

		public void print() {
			System.out.println("Type: " + item_type + ", Name: " + item_text);
			System.out.println("<" + (noedit?"NoEdit":"Edit") + "," + "CopyRatio=" + copyratio + ">");
			
		}
		
		public void copyInfo(TemplateItem ti) {
			this.copyratio = ti.copyratio;
			this.word_max = ti.word_max;
			this.word_min = ti.word_min;
			this.pics_max = ti.pics_max;
			this.pics_min = ti.pics_min;
			this.pages_max = ti.pages_max;
			this.pages_min = ti.pages_min;
		}
		
		@Override
		public String toString() {
			return "TI<" + index_level + "/" + index_no + "/" + tino + ">";
		}

		@Override
		public int compareTo(TemplateItem ti) {
			if (this == ti)
				return 0;
			return tino - ti.tino;
//			else if (index_level == 0)
//				return -1;
//			else if (ti.index_level == 0)
//				return 1;
//			else if (index_level == ti.index_level && index_level < 100) {
//				if (parent == ti.parent)
//					return index_no - ti.index_no;
//				else
//					return this.parent.compareTo(ti.parent);
//			}
//			else if (index_level == ti.index_level && index_level == 100)
//				return this.parent.compareTo(ti.parent);
//			else if (index_level > ti.index_level)
//				return this.parent.compareTo(ti);
//			else
//				return compareTo(ti.parent);
		}
	};
	
	public static final char cardinals[][] = {{'1', '一'}, {'2', '二'}, {'3', '三'}, 
		{'4', '四'}, {'5', '五'}, {'6', '六'}, {'7', '七'}, {'8', '八'}, {'9', '九'}} ;
	public static final char delim[] = {'．', '、', '.', '。', ' '};
	public static final int PREFIX_LEN = 4;

	private TemplateItem root;
	private HashMap<String,Vector<TemplateItem>> hmap;
	private Pattern ptn_words = Pattern.compile(TemplateItem.Tag_Words_Pattern);
	private Pattern ptn_pics = Pattern.compile(TemplateItem.Tag_Pictures_Pattern);
	private Pattern ptn_pages = Pattern.compile(TemplateItem.Tag_Pages_Pattern);
	private Pattern ptn_appd = Pattern.compile(TemplateItem.Tag_Appendix_Pattern);
	
	public Vector<String> getIdInfoStrings() {
		Vector<String> idinfos = new Vector<String> ();
		for (TemplateItem ti: root.children) {
			if (ti.item_type != ItemType.TI_TYPE_INFO)
				break;
			String idi = ti.item_text.replaceAll(" \t\r\n", "");
			String []toks = idi.split("：");
			idinfos.add(toks[0]+"：");
		}
		return idinfos;
	}
	
	public static boolean isCardinal(char ch) {
		for (int i = 0; i < cardinals.length; ++i) {
			if (ch == cardinals[i][0] || ch == cardinals[i][1])
				return true;
		}

		return false;
	}

	public static boolean isDelim(char ch) {
		for (char de: delim) {
			if (ch == de)
				return true;
		}
		return false;
	}

	public TemplateItem getRoot() {
		return root;
	}

	// parse a line like '[<Tags>] xxx .... <template item tags ...>'
	private TemplateItem parseTemplateLine(String line) {
		TemplateItem ti = new TemplateItem();
		if (line.charAt(0) == '<') {
			int idx = line.indexOf('>');
			String heading = line.substring(1, idx);
			line = line.substring(idx+1);
			if (heading.startsWith("NewPage"))
				ti.item_type = ItemType.TI_TYPE_NEWPAGE;
			else if (heading.startsWith("标题 ") || heading.startsWith("heading ")) {
				// <heading X>
				ti.heading_no = Integer.parseInt(heading.substring(heading.length()-1));
			}
		}
		String []items = line.split("<", 2);
		ti.item_text = items[0];
		
		// [hints: ..., 字数：80-200, 图：1个, 页数：2-10页 ...]
		if (ti.item_text.startsWith("[") && ti.item_text.contains("]")) {
			ti.item_type = ItemType.TI_TYPE_HINT;
			Matcher m = ptn_words.matcher(ti.item_text);
			if (m.find()) {
				ti.word_min = Integer.parseInt(m.group(1));
				ti.word_max = Integer.parseInt(m.group(2));
			}
			m = ptn_pics.matcher(ti.item_text);
			if (m.find()) {
				ti.pics_min = Integer.parseInt(m.group(1));
				if (m.groupCount() == 2)
					ti.pics_max = Integer.parseInt(m.group(2));
				else {
					if (m.group(0).endsWith("+")) {
						ti.pics_max = (ti.pics_min > 5)?ti.pics_min*4:ti.pics_min*8;
					}
					else
						ti.pics_max = ti.pics_min;
				}
			}
			m = ptn_pages.matcher(ti.item_text);
			if (m.find()) {
				ti.pages_min = Integer.parseInt(m.group(1));
				if (m.groupCount() == 2)
					ti.pages_max = Integer.parseInt(m.group(2));
				else {
					if (m.group(0).endsWith("+")) {
						ti.pages_max = (ti.pages_min > 5)?ti.pages_min*2:ti.pages_min*4;
					}
					else
						ti.pages_max = ti.pages_min;
				}
			}
		}
		else if (ItemType.TI_TYPE_NEWPAGE != ti.item_type)
			ti.item_type = ItemType.TI_TYPE_FIXED_TEXT;
		
		if (items.length < 2)
			return ti;
		// it should ends with '>', so len-1
		String []tags = items[1].substring(0, items[1].length()-1).split(",");
		for (String tag: tags) {
			if (tag.equalsIgnoreCase(TemplateItem.Tag_Index0)) {
				ti.item_type = ItemType.TI_TYPE_INDEX;
				ti.index_level = 0;
			}
			else if (tag.equalsIgnoreCase(TemplateItem.Tag_Index1)) {
				ti.item_type = ItemType.TI_TYPE_INDEX;
				ti.index_level = 1;
				Matcher m = ptn_appd.matcher(ti.item_text);
				
				ti.isappendix = (m != null && m.matches());
			}
			else if (tag.equalsIgnoreCase(TemplateItem.Tag_Index2)) {
				ti.item_type = ItemType.TI_TYPE_INDEX;
				ti.index_level = 2;
			}
			else if (tag.equalsIgnoreCase(TemplateItem.Tag_IdInfo)) {
				ti.item_type = ItemType.TI_TYPE_INFO;
				ti.index_level = 1;
			}
			else if (tag.equalsIgnoreCase(TemplateItem.Tag_DateInfo)) {
				ti.item_type = ItemType.TI_TYPE_INFO;
				ti.index_level = 1;
			}
			else if (tag.equalsIgnoreCase(TemplateItem.Tag_NoEdit)) {
				ti.noedit = true;
			}
			else if (tag.equalsIgnoreCase(TemplateItem.Tag_Mandated)) {
				ti.mandated = true;
			}
			else if (tag.startsWith (TemplateItem.Tag_CopyRatio)) {
				String []pv = tag.split("=", 2);
				ti.copyratio = Double.parseDouble(pv[1]);
			}
		}
		ti.children = new ArrayList<TemplateItem> ();
		
		return ti;
	}

	//返回串
	private static String getPrefix(String line) {
		StringBuilder prefix = new StringBuilder(PREFIX_LEN);
		int be = 0;

//		if (line.charAt(0) == '<') {
//			int idx = line.indexOf('>');
//			assert(idx > 0);
//			line = line.substring(idx + 1);
//		}

		if (line.length() <= PREFIX_LEN)
			prefix.append(line);
		else {
			if (isCardinal(line.charAt(0)) && isDelim(line.charAt(1))) //都是在读取
				be = 2;
			for (int i = be, len = 0;i < line.length() && len < PREFIX_LEN; ++i) {
				char c = line.charAt(i);
				if (!Character.isWhitespace(c) && !Character.isSpaceChar(c)) {
					prefix.append(c);
					++ len;
					if (c == ':' || c == '：') // 解决 "姓名：XXX"问题
						break;
				}
			}
		}

		return prefix.toString();
	}
	// Bind string(0,4) to its TemplateItem
	//  i.e., bind '实验一、' -> {'', ..., '实验一、基于AVR ATMega128的硬件(PCB)设计<NoEdit,Index1,Mandated>'}
	// if string like '1. xxxxxx' then only 'xxxxxx' taken
	private void bind(String line, TemplateItem ti) {
		String prefix = getPrefix(line);
		Vector<TemplateItem> v = hmap.get(prefix);
		if (v == null)
			v = new Vector<TemplateItem>();
		v.add(ti);
		hmap.put(prefix, v);
	}

	public Template(String tempfile) {
		hmap = new HashMap<String, Vector<TemplateItem>>(128);
		Stack<TemplateItem> temptree = new Stack<TemplateItem>();
		boolean head = true;
		TemplateItem ti = null;
		int tino = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempfile), StandardCharsets.UTF_8));

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() <= 3)
					continue;

				ti = parseTemplateLine(line.trim());
				ti.tino = tino ++;
				if (head) {
					root = ti;
					root.head = true;
					temptree.push(root);
					head = false;
				}
				else {
					TemplateItem lastti = temptree.peek();
					
					for (;lastti.index_level > ti.index_level;lastti = temptree.peek()) {
						temptree.pop();
					}
					if (lastti.index_level == ti.index_level) {
						lastti.parent.children.add(ti);
						ti.parent = lastti.parent;
						ti.index_no = ti.parent.children.size() - 1;
						temptree.pop();
						temptree.push(ti);
					}
					else if (lastti.index_level < ti.index_level) {
						if (ti.item_type == ItemType.TI_TYPE_HINT)
							lastti.copyInfo(ti);
						lastti.children.add(ti);
						ti.parent = lastti;
						ti.index_no = ti.parent.children.size() - 1;
						temptree.push(ti);
					}
				}
				bind(line, ti);
			}
			br.close();
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	
	public Vector<TemplateItem> getMatchedItems(String line) {
		return hmap.get(getPrefix(line));
	}

	public void print() {
		root.print();
		for (TemplateItem idx1: root.children) {
			idx1.print();
			for (TemplateItem idx2: idx1.children) {
				idx2.print();
				for (TemplateItem ti: idx2.children)
					ti.print();
			}
		}
	}

//	public static void main(String[] args) {
//		if (args.length < 1) {
//			System.err.println("Usage: java org.xjtu.Template filename");
//			return;
//		}
//		try {
//		Template t = new Template(args[0]);
//		t.print();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return;
//	}
}
