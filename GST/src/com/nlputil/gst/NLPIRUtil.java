package com.nlputil.gst;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public class NLPIRUtil {
    public interface NLPIRInterface extends Library {
                NLPIRInterface INSTANCE = (NLPIRInterface)Native.loadLibrary("E:\\ICTclas\\lib\\win64\\NLPIR", NLPIRInterface.class);

                /**
                 * ��ʼ���ִʹ���
                 * @param sDataPath���ִʹ������Ͽ�·��
                 * @param encode ����,����
                 * GBK����  0
                 * UTF-8  1
                 * BIG5   2
                 * GBK�ͷ��� 3
                 * @param sLicenceCode  ��Ȩ���롡��Ĭ��Ϊ0
                 * @return boolean ��ʼ���ɹ�����true����ʼ��ʧ�ܷ���false
                 */
                public boolean NLPIR_Init(String sDataPath, int encode, String sLicenceCode);

                /**
                 * �˳��ִʹ��ߣ��ͷŻ���
                 * @return boolean �˳��ɹ�����true���˳�ʧ�ܷ���false
                 */
                public boolean NLPIR_Exit();

                /**
                 * �ı�����
                 * @param sParagraph �ı���Ϣ
                 * @param bPOStagged�����Ա�ע��0������ע��1��ע
                 * @return String �����ı�����������ַ���
                 */
                public String NLPIR_ParagraphProcess(String sParagraph, int bPOStagged);

                /**
                 * �߼��ļ�����(���Ա�ע�ִ�λ��)
                 * @param sParagraph �ı���Ϣ
                 * @param pResultCount ָ��ĳһ�ִʵ��α�
                 * @param useDict �Ƿ�ʹ���û��Զ�������
                 * @return result_t
                 */
                public result_t NLPIR_ParagraphProcessA(String sParagraph, IntByReference pResultCount, boolean useDict);

                /**
                 * �ı������󷵻صĴʻ�����
                 * @param sParagraph���ı���Ϣ
                 * @return int �ʻ�����
                 */
                public int NLPIR_GetParagraphProcessAWordCount(String sParagraph);

                /**
                 * �ļ�����
                 * @param sSourceFilename����Ҫ�������ļ�·��
                 * @param sResultFilename���ļ��������ŷ���������ļ�·��
                 * @param bPOStagged        ���Ա�ע��0����ע��1��ע
                 * @return double �����ɹ����ء�true ����ʧ�ܷ���false
                 */
                public double NLPIR_FileProcess(String sSourceFilename, String sResultFilename, int bPOStagged);

                /**
                 * �����ⲿ�û��ʵ��ļ����ļ��дʵ��ʽΪ��ÿ���ʻ�ռһ�У�ÿ��Ϊ�ʻ�ʹ����м��ÿո�����������þ� n
                 * @param sFilename���ⲿ�û��ʵ�·��
                 * @return ���ص���ɹ��Ĵʻ�����
                 */
                public  int NLPIR_ImportUserDict(String sFilename);

                /**
                 * ��̬���شʻ�
                 * @param sWord���ʻ㣬��ʽΪ�ʻ�ʹ����м��ÿո����
                 * @return int ����1�����سɹ�������0������ʧ��
                 */
                public int NLPIR_AddUserWord(String sWord);

                /**
                 * �����û��Զ��嵼���ֵ�
                 * @return int ����ɹ���1 ��������ʧ�ܡ�2
                 */
                public int NLPIR_SaveTheUsrDic();

                /**
                 * ɾ���������Ͽ��еĴʻ�
                 * @param sWord���ʻ�
                 * @return int ������û�иôʻ㷵��-1
                 */
                public int NLPIR_DelUsrWord(String sWord);

                /**
                 * ��ȡ�ִʸ���
                 * @param sWord ����
                 * @return  �ִʸ���
                 */
                public double NLPIR_GetUniProb(String sWord);

                /**
                 * �жϴʻ��Ƿ��ں��Ŀ���
                 * @param sWord���ʻ�
                 * @return boolean �����ڷ��ء�true�����򷵻�false
                 */
                public boolean NLPIR_IsWord(String sWord);

                /**
                 * ���ַ�������ȡ�ؼ���
                 * @param sLine  �ı���Ϣ
                 * @param nMaxKeyLimit ���ؼ��ʳ���
                 * @param bWeightOut �Ƿ�����Ȩ����Ϣ
                 * @return String ���ؽ���ַ���
                 */
                public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit, boolean bWeightOut);

                /**
                 * ���ı��ļ�����ȡ�ؼ���
                 * @param sFilename �ı��ļ�·��
                 * @param nMaxKeyLimit ���ؼ��ʳ���
                 * @param bWeightOut �Ƿ�����Ȩ����Ϣ
                 * @return ���ؽ���ַ���
                 */
                public String NLPIR_GetFileKeyWords(String sFilename, int nMaxKeyLimit, boolean bWeightOut);

                /**
                 * ���ַ����з����´�
                 * @param sFilename �ı���Ϣ
                 * @param nMaxKeyLimit ���ؼ��ʳ���
                 * @param bWeightOut �Ƿ�����Ȩ����Ϣ
                 * @return ���ؽ���ַ���
                 */
                public String NLPIR_GetNewWords(String sFilename, int nMaxKeyLimit, boolean bWeightOut);

                /**
                 * ���ı��ļ��з����´�
                 * @param sFilename �ı��ļ�·��
                 * @param nMaxKeyLimit ���ؼ��ʳ���
                 * @param bWeightOut �Ƿ�����Ȩ����Ϣ
                 * @return ���ؽ���ַ���
                 */
                public String NLPIR_GetFileNewWords(String sFilename, int nMaxKeyLimit, boolean bWeightOut);

                /**
                 * ���ô��Ա�עӳ��
                 * @param nPOSmap�����Ա�עӳ������
                 * ������һ����ע��  1
                 * ������������ע��  0
                 * ���������ע��  2
                 * ����һ����ע��  3
                 * @return int  ���óɹ�����1����ʧ�ܷ���0
                 */
                public int NLPIR_SetPOSmap(int nPOSmap);

                /**
                 * �ִʽ��ʵ��
                 * @author Ricky
                 */
                public class result_t extends Structure {
                        public int start;//��Ԫ����������еĿ�ʼλ��
                        public int length;//��Ԫ����
                        public byte[] sPOS = new byte[40];//��Ԫ����
                        public int iPOS;//���Ա�ע�ı��
                        public int word_ID;//�ôʵ��ڲ�ID�ţ������δ��¼�ʣ����0����-1
                        public int word_type; //�����û��ʵ�;1�����û��ʵ��еĴʣ�0�����û��ʵ��еĴ�
                        public int weight;//word weight,read weight
                        public result_t() {}
                        protected List<? > getFieldOrder() {
                                return Arrays.asList("start", "length", "sPOS", "iPOS", "word_ID", "word_type", "weight");
                        }
                        public result_t(int start, int length, byte sPOS[], int iPOS, int word_ID, int word_type, int weight) {
                                super();
                                this.start = start;
                                this.length = length;
                                if ((sPOS.length != this.sPOS.length))
                                        throw new IllegalArgumentException("Wrong array size !");
                                this.sPOS = sPOS;
                                this.iPOS = iPOS;
                                this.word_ID = word_ID;
                                this.word_type = word_type;
                                this.weight = weight;
                       }
                }
        }




        static{

                NLPIR_Init();
                NLPIR_SetPOSmap(0);
  //      NLPIRUtil.initTimeCount += (System.currentTimeMillis() - t);
        }
        public static boolean NLPIR_Init(){
                //��ʼ���п�Ժ�ִ���
                boolean initStatus = NLPIRInterface.INSTANCE.NLPIR_Init("E:\\ICTclas",1/* XMLUtil.DEFAULT_CODING*/, "0");
                 if(initStatus == true){
                     //    System.out.println("�п�Ժ�ִ���======>��ʼ���ɹ���");
                 }else{
                         System.err.println("�п�Ժ�ִ���======>��ʼ��ʧ�ܡ�");
                 }
                 return initStatus;
        }

        public static boolean NLPIR_Exit(){
                //��ʼ���п�Ժ�ִ���
                boolean exitStatus = NLPIRInterface.INSTANCE.NLPIR_Exit();
                 if(exitStatus == true){
                     //    System.out.println("�п�Ժ�ִ���======>�ͷ���Դ�ɹ���");
                 }else{
                         System.err.println("�п�Ժ�ִ���======>�ͷ���Դʧ�ܡ�");
                 }
                 return exitStatus;
        }

        /**
         * �ı�����
         * @param paragraph �ı���Ϣ
         * @param bPOStagged�����Ա�ע��0������ע��1��ע
         * @return String �����ı�����������ַ���
         */
        public static String NLPIR_ParagraphProcess(String paragraph,int bPOStagged){
                String output = NLPIRInterface.INSTANCE.NLPIR_ParagraphProcess(paragraph, bPOStagged);
                return output;
        }


        /**
         * �ı�����
         * @param paragraph ��������
         * @return result_t[]
         */
        public static NLPIRInterface.result_t[] NLPIR_ParagraphProcessA(String paragraph){
                 IntByReference pResultCount = new IntByReference();
                 NLPIRInterface.result_t results = NLPIRInterface.INSTANCE.NLPIR_ParagraphProcessA(paragraph, pResultCount, true);
         		int nc = pResultCount.getValue();
        		if (nc > 0)
        			return (NLPIRInterface.result_t[]) results.toArray(nc);
        		else
        			return null;
             /*    NLPIRInterface.result_t[] resultArray = (NLPIRInterface.result_t[])results.toArray(pResultCount.getValue());
                 LinkedList<NLPIRInterface.result_t> list = new LinkedList<NLPIRInterface.result_t>();
                 for(NLPIRInterface.result_t t:resultArray){
                         list.add(t);
                 }
                 return list;*/
        }




        /**
         * �ı������󷵻صĴʻ�����
         * @param sParagraph���ı���Ϣ
         * @return int �ʻ�����
         */
        public static int NLPIR_GetParagraphProcessAWordCount(String sParagraph){
                return  NLPIRInterface.INSTANCE.NLPIR_GetParagraphProcessAWordCount(sParagraph);

        }

        /**
         * �ļ�����
         * @param sSourceFilename����Ҫ�������ļ�·��
         * @param sResultFilename���ļ��������ŷ���������ļ�·��
         * @param bPOStagged        ���Ա�ע��0����ע��1��ע
         * @return boolean �����ɹ����ء�true ����ʧ�ܷ���false
         */
        public static double NLPIR_FileProcess(String sSourceFilename,String sResultFilename,int bPOStagged){
                return NLPIRInterface.INSTANCE.NLPIR_FileProcess(sSourceFilename, sResultFilename, bPOStagged);
        }

        /**
         * �����ⲿ�û��ʵ��ļ����ļ��дʵ��ʽΪ��ÿ���ʻ�ռһ�У�ÿ��Ϊ�ʻ�ʹ����м��ÿո�����������þ� n
         * @param sFilename���ⲿ�û��ʵ�·��
         * @return ���ص���ɹ��Ĵʻ�����
         */
        public static int NLPIR_ImportUserDict(String sFilename){
                return  NLPIRInterface.INSTANCE.NLPIR_ImportUserDict(sFilename);

        }

        /**
         * ��̬���شʻ�
         * @param sWord���ʻ㣬��ʽΪ�ʻ�ʹ����м��ÿո����
         * @return int ����1�����سɹ�������0������ʧ��
         */
        public static int NLPIR_AddUserWord(String sWord){
                return  NLPIRInterface.INSTANCE.NLPIR_AddUserWord(sWord);

        }

        /**
         * �����û��Զ��嵼���ֵ�
         * @return int ����ɹ���1 ��������ʧ�ܡ�2
         */
        public static int NLPIR_SaveTheUsrDic(){
                return  NLPIRInterface.INSTANCE.NLPIR_SaveTheUsrDic();
        }

        /**
         * ɾ���������Ͽ��еĴʻ�
         * @param sWord���ʻ�
         * @return int ������û�иôʻ㷵��-1
         */
        public static int NLPIR_DelUsrWord(String sWord){
                return  NLPIRInterface.INSTANCE.NLPIR_DelUsrWord(sWord);

        }

        /**
         * ��ȡ�ִʸ���
         * @param sWord ����
         * @return �ִʸ���
         */
        public static double NLPIR_GetUniProb(String sWord){
                return  NLPIRInterface.INSTANCE.NLPIR_GetUniProb(sWord);

        }

        /**
         * �жϴʻ��Ƿ��ں��Ŀ���
         * @param sWord���ʻ�
         * @return boolean �����ڷ��ء�true�����򷵻�false
         */
        public static boolean NLPIR_IsWord(String sWord){
                return  NLPIRInterface.INSTANCE.NLPIR_IsWord(sWord);
        }

        /**
         * ���ַ�������ȡ�ؼ���
         * @param sLine  �ı���Ϣ
         * @param nMaxKeyLimit ���ؼ��ʳ���
         * @param bWeightOut �Ƿ�����Ȩ����Ϣ
         * @return String ���ؽ���ַ���
         */
        public static String NLPIR_GetKeyWords(String sLine,int nMaxKeyLimit,boolean bWeightOut){
                return  NLPIRInterface.INSTANCE.NLPIR_GetKeyWords(sLine, nMaxKeyLimit, bWeightOut);

        }

        /**
         * ���ı��ļ�����ȡ�ؼ���
         * @param sFilename �ı��ļ�·��
         * @param nMaxKeyLimit ���ؼ��ʸ���
         * @param bWeightOut �Ƿ�����Ȩ����Ϣ
         * @return ���ؽ���ַ���
         */
        public static String NLPIR_GetFileKeyWords(String sFilename,int nMaxKeyLimit,boolean bWeightOut){
                return  NLPIRInterface.INSTANCE.NLPIR_GetFileKeyWords(sFilename, nMaxKeyLimit, bWeightOut);

        }

        /**
         * ���ַ����з����´�
         * @param sLine �ı���Ϣ
         * @param nMaxKeyLimit ���ؼ��ʸ���
         * @param bWeightOut �Ƿ�����Ȩ����Ϣ
         * @return ���ؽ���ַ���
         */
        public static String NLPIR_GetNewWords(String sLine,int nMaxKeyLimit,boolean bWeightOut){
                return  NLPIRInterface.INSTANCE.NLPIR_GetNewWords(sLine, nMaxKeyLimit, bWeightOut);

        }

        /**
         * ���ı��ļ��з����´�
         * @param sFilename �ı��ļ�·��
         * @param nMaxKeyLimit ���ؼ��ʸ���
         * @param bWeightOut �Ƿ�����Ȩ����Ϣ
         * @return ���ؽ���ַ���
         */
        public static String NLPIR_GetFileNewWords(String sFilename,int nMaxKeyLimit,boolean bWeightOut){
                return  NLPIRInterface.INSTANCE.NLPIR_GetFileNewWords(sFilename, nMaxKeyLimit, bWeightOut);

        }

        /**
         * ���ô��Ա�עӳ��
         * @param nPOSmap�����Ա�עӳ������
         * ������һ����ע��  1
         * ������������ע��  0
         * ���������ע��  2
         * ����һ����ע��  3
         * @return int  ���óɹ�����1����ʧ�ܷ���0
         */
        public static int NLPIR_SetPOSmap(int nPOSmap){
                int posMap = NLPIRInterface.INSTANCE.NLPIR_SetPOSmap(nPOSmap);
                return posMap;
        }
}
