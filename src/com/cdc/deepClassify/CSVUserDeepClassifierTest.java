package com.cdc.deepClassify;



import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.junit.Test;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.lingjoin.fileutil.FileOperateUtils;

public class CSVUserDeepClassifierTest {
	
	private int DC_ENCODING = 1;
	private String DC_ENCODING_STRING = "utf-8";
	private int DC_FEATURE_COUNT = 802;
	
	private String CSV_SEP = "\t";
	private int CSV_INDEX_MSISDN = 0;
	private int CSV_INDEX_SEX = 1;
	private int CSV_INDEX_AGE = 2;
	private int CSV_INDEX_VALUE = 3;
//	private int CSV_INDEX_CLASSIFY = 4;
	
	private ArrayList<ClassifyUser> trainList = new ArrayList<ClassifyUser>();
	private ArrayList<ClassifyUser> testList = new ArrayList<ClassifyUser>();
	
	private String DC_TRAIN_PATH = "csv/train";
	private String DC_TEST_PATH = "csv/test";
	
	/***********************************************
	 * 重要：使用前，CSV请按性别、年龄、MSISDN、Value进行排序*
	 ***********************************************/
	
	/**
	 * 加载数据
	 */
	public void loadData() throws IOException{
		CsvReader csvReader;
		ArrayList list;
		ClassifyUser user;
		File file;
		// 加载训练数据
		trainList.clear();
		FileOperateUtils.clearFilesList();
		list = FileOperateUtils.getAllFilesPath(new File(DC_TRAIN_PATH));
		user = null;
		for (int i = 0; i < list.size(); i++) {
			file = new File(list.get(i).toString());
			// 忽略非CSV文件
			if (file.getName().toLowerCase().indexOf(".csv") < 0) continue;
			//将训练分类文本加载到内存中
			csvReader = new CsvReader(file.getPath(), CSV_SEP.charAt(0), Charset.forName(DC_ENCODING_STRING));
			long lIndex = 0;
			while (csvReader.readRecord()) {
				String[] rows = csvReader.getRawRecord().split(CSV_SEP);
				lIndex++;
				if (rows.length == 4){
					if ((user = ClassifyUser.isNewUser(
							rows[CSV_INDEX_MSISDN],
							rows[CSV_INDEX_SEX],
							rows[CSV_INDEX_AGE],
							rows[CSV_INDEX_VALUE])) != null){
						trainList.add(user);
					}
				} else {
					System.out.println("file:" + file.getPath() + ", index:" + lIndex + ", len:" + rows.length);
					System.exit(1);
				}
			}
			trainList.add(ClassifyUser.getUser());
			csvReader.close();
		}
		// 加载测试数据
		testList.clear();
		FileOperateUtils.clearFilesList();
		list = FileOperateUtils.getAllFilesPath(new File(DC_TEST_PATH));
		user = null;
		for (int i = 0; i < list.size(); i++) {
			file = new File(list.get(i).toString());
			// 忽略非CSV文件
			if (file.getName().toLowerCase().indexOf(".csv") < 0) continue;
			//将测试分类文本加载到内存中
			csvReader = new CsvReader(file.getPath(), CSV_SEP.charAt(0), Charset.forName(DC_ENCODING_STRING));
			long lIndex = 0;
			while (csvReader.readRecord()) {
				String[] rows = csvReader.getRawRecord().split(CSV_SEP);
				lIndex++;
				if (rows.length == 4){
					if ((user = ClassifyUser.isNewUser(
							rows[CSV_INDEX_MSISDN],
							rows[CSV_INDEX_SEX],
							rows[CSV_INDEX_AGE],
							rows[CSV_INDEX_VALUE])) != null){
						testList.add(user);
					}
				} else {
					System.out.println("file:" + file.getPath() + ", index:" + lIndex + ", len:" + rows.length);
//					System.exit(1);
				}
			}
			testList.add(ClassifyUser.getUser());
			csvReader.close();
		}
	}

	/**
	 * 训练过程
	 * @throws IOException 
	 */
	@Test
	public void didTrain(int v) throws IOException {
		//1、训练过程--初始化
		boolean flag = DeepClassifierLibrary.Instance.DC_Init("", DC_ENCODING, DC_FEATURE_COUNT, "");
		if (flag) {
			System.out.println("deepClassifier初始化成功");
		} else {
			System.out.println("deepClassifier初始化失败：" + DeepClassifierLibrary.Instance.DC_GetLastErrorMsg());
			System.exit(1);
		}
		
		//2、训练过程--训练数据划分
		ClassifyData data = new ClassifyData(v);
		ClassifyUser user;
		for(int i = 0,l = trainList.size(); i < l; i++){
			user = trainList.get(i);
			data.put(user.labels.get("sex"), user.value);
		}

		//2、训练过程--导入数据
		for(String label : data.getLabels()){
			String[] values = data.getValues(label);
			for(int i = 0; i < values.length; i++){
				if (values[i] != null)
					DeepClassifierLibrary.Instance.DC_AddTrain(
						label, values[i]);
			}
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
	public double[] didClassify() throws IOException {
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
		ClassifyUser user;
		for (int i = 0; i < testList.size(); i++) {
			user = testList.get(i);
			String classify = DeepClassifierLibrary.Instance.DC_Classify(user.value);
			// 判断分类准确性
			String sex = user.labels.get("sex");
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
		
		return new double[]{
				(double)mRLen/mLen, 
				(double)wRLen/wLen,
				(double)fRLen/fLen
		};
	}
	
	public static void main(String avgs[]){
		CSVUserDeepClassifierTest test = new CSVUserDeepClassifierTest();
		double bestRate = 0;
		double bestManRate = 0;
		double bestWomanRate = 0;
		double bestTotalRate = 0;
		double bestMinRate = 0;
		String result = "";
		int bestV = 0;
		try{
			test.loadData();
//			test.didTrain(520);
//			test.didClassify();
			for(int i = 500; i <= 700; i=i+1) {
				test.didTrain(i);
				double[] rate = test.didClassify();
				double minRate =  Math.min(rate[0], rate[1]);
				if (minRate - bestMinRate >= 0.01){
					bestMinRate = minRate;
				}
				if (bestMinRate - minRate < 0.01 && 
					    		rate[0] + rate[1] > bestRate) {
					bestRate = rate[0] + rate[1];
					bestManRate = rate[0];
					bestWomanRate = rate[1];
					bestTotalRate = rate[2];
					bestV = i;
				}
				result += i + "," + rate[0] + "," + rate[1] + "," + rate[2] + "\n";
				System.out.println("bestManRate:" + bestManRate);
				System.out.println("bestWomanRate:" + bestWomanRate);
				System.out.println("bestTotalRate:" + bestTotalRate);
				System.out.println("bestV:" + bestV);
				System.out.println("currV:" + i);
			}
			result += bestV + "," + bestManRate + "," + bestWomanRate + "," + bestTotalRate;
			FileOperateUtils.writeFile(result, "F:\\Projects\\deepclassify\\result.txt");
			System.out.println("bestV:" + bestV);
			System.out.println("bestManRate:" + bestManRate);
			System.out.println("bestWomanRate:" + bestWomanRate);
			System.out.println("bestTotalRate:" + bestTotalRate);
			System.out.println("bestRate:" + bestRate);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
