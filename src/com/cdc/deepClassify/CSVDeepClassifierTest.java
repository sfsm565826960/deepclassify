package com.cdc.deepClassify;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.lingjoin.fileutil.FileOperateUtils;

public class CSVDeepClassifierTest {
	
	private int DC_ENCODING = 1;
	private String DC_ENCODING_STRING = "utf-8";
	private int DC_FEATURE_COUNT = 802;
	
	private String CSV_SEP = "\t";
	private int CSV_INDEX_MSISDN = 0;
	private int CSV_INDEX_SEX = 1;
	private int CSV_INDEX_AGE = 2;
	private int CSV_INDEX_VALUE = 3;
//	private int CSV_INDEX_CLASSIFY = 4;
	
	private String DC_TRAIN_PATH = "csv/train";
	private String DC_TEST_PATH = "csv/test";

	/**
	 * 训练过程
	 * @throws IOException 
	 */
	@Test
	public void didTrain() throws IOException {
		//1、训练过程--初始化
		boolean flag = DeepClassifierLibrary.Instance.DC_Init("", DC_ENCODING, DC_FEATURE_COUNT, "");
		if (flag) {
			System.out.println("deepClassifier初始化成功");
		} else {
			System.out.println("deepClassifier初始化失败：" + DeepClassifierLibrary.Instance.DC_GetLastErrorMsg());
			System.exit(1);
		}
		//2、训练过程--遍历训练分类文本的CSV文件
		ArrayList list = FileOperateUtils.getAllFilesPath(new File(DC_TRAIN_PATH));
		CsvReader csvReader;
		ClassifySexAge classifySexAge = new ClassifySexAge();
		for (int i = 0; i < list.size(); i++) {
			File f = new File(list.get(i).toString());
			// 忽略非CSV文件
			if (f.getName().toLowerCase().indexOf(".csv") < 0) continue;
			//将训练分类文本加载到内存中
			csvReader = new CsvReader(f.getPath(), CSV_SEP.charAt(0), Charset.forName(DC_ENCODING_STRING));
			int lIndex = 0;
			while (csvReader.readRecord()) {
				String[] rows = csvReader.getRawRecord().split(CSV_SEP);
				lIndex++;
				if (rows.length == 4){
					DeepClassifierLibrary.Instance.DC_AddTrain(
							rows[CSV_INDEX_SEX], rows[CSV_INDEX_VALUE]);
				} else {
					System.out.println("file:" + f.getPath() + ", index:" + lIndex + ", len:" + rows.length);
					System.exit(1);
				}
			}
			csvReader.close();
		}
		//3、训练过程--开始训练
		DeepClassifierLibrary.Instance.DC_Train();
		//4、训练过程--训练结束，退出
		DeepClassifierLibrary.Instance.DC_Exit();
	}
	
	/**
	 * 分类过程
	 * @throws IOException
	 */
	@Test
	public void didClassify() throws IOException {
		long fLen = 0; // 总行数
		long fRLen = 0; // 总准确
		long mLen = 0; // 男性
		long mRLen = 0; // 男性准确
		long wLen = 0; // 女性
		long wRLen = 0; // 女性准确
		long iLen = 0; // 无效
		long iRLen = 0; // 无效行数
		long lMLen = 0; // 女疑男
		long lWLen = 0; // 男疑女
		long lILen = 0; // 疑无效
		
		//1、分类过程--初始化
		if (DeepClassifierLibrary.Instance.DC_Init("", DC_ENCODING, DC_FEATURE_COUNT, "")) {
			System.out.println("deepClassifier初始化成功");
		} else {
			System.out.println("deepClassifier初始化失败：" + DeepClassifierLibrary.Instance.DC_GetLastErrorMsg());
			System.exit(1);
		}
		//2、分类过程--加载训练结果
		DeepClassifierLibrary.Instance.DC_LoadTrainResult();
		
		//3、分类过程--读取待分类的文本
		FileOperateUtils.clearFilesList();
		File path = new File(DC_TEST_PATH);
		ArrayList list = FileOperateUtils.getAllFilesPath(path);
		CsvReader csvReader;
		CsvWriter csvWriter;
		for (int i = 0; i < list.size(); i++) {
			File f = new File(list.get(i).toString());
			// 忽略非CSV文件
			if (f.getName().toLowerCase().indexOf(".csv") < 0) continue;
			//将训练分类文本加载到内存中
			csvReader = new CsvReader(f.getPath(), CSV_SEP.charAt(0), Charset.forName(DC_ENCODING_STRING));
			csvWriter = new CsvWriter(new File(f.getParent(), "classify_" + f.getName()).getPath(), CSV_SEP.charAt(0), Charset.forName(DC_ENCODING_STRING));
			// 开始分析
			while (csvReader.readRecord()) {
				String[] rows = csvReader.getRawRecord().split(CSV_SEP);
				if (rows.length != 4){
					System.out.print(rows);
					continue;
				}
				// 分析类别
				String classify = DeepClassifierLibrary.Instance.DC_Classify(rows[CSV_INDEX_VALUE]);
				// 输出分类
				String[] record = new String[5];
				System.arraycopy(rows, 0, record, 1, rows.length);
				record[0] = classify;
				csvWriter.writeRecord(record);
				// 判断分类准确性
				String sex = rows[CSV_INDEX_SEX];
				fLen++;
				if ("男".equals(sex)) {
					mLen++;
				} else if ("女".equals(sex)) {
					wLen++;
				} else if ("无效".equals(sex)) {
					iLen++;
				}
				if (sex.equals(classify)) {
					if ("男".equals(sex)) {
						mRLen++;
					} else if ("女".equals(sex)) {
						wRLen++;
					} else if ("无效".equals(classify)) {
						iRLen++;
					}
					fRLen++;
				} else {
					if ("男".equals(classify)) {
						lMLen++;
					} else if ("女".equals(classify)) {
						lWLen++;
					} else if ("无效".equals(classify)) {
						lILen++;
					}
				}
			}
			csvWriter.close();
			csvReader.close();	
		}
		//4、统计结果
		System.out.println("--------------异议统计----------------");
		System.out.println("男疑女：" + lWLen);
		System.out.println("女疑男：" + lMLen);
		System.out.println("疑无效：" + lILen);
		System.out.println("--------------总体统计----------------");
		System.out.println("男性：" + mRLen + "/" + mLen + "(" + ((double)mRLen/mLen) + ")");
		System.out.println("女性：" + wRLen + "/" + wLen + "(" + ((double)wRLen/wLen) + ")");
		System.out.println("无效：" + iRLen + "/" + iLen + "(" + ((double)iRLen/iLen) + ")");
		System.out.println("总数：" + fRLen + "/" + fLen + "(" + ((double)fRLen/fLen) + ")");
					
		//5、分类过程--退出
		DeepClassifierLibrary.Instance.DC_Exit();
	}
	
	public static void main(String avgs[]){
		CSVDeepClassifierTest test = new CSVDeepClassifierTest();
		try{
			test.didTrain();
			test.didClassify();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
