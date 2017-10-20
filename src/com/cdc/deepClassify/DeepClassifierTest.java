package com.cdc.deepClassify;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.lingjoin.fileutil.FileOperateUtils;

public class DeepClassifierTest {
	
	private int DC_ENCODING = 1;
	private String DC_ENCODING_STRING = "utf-8";
	private int DC_FEATURE_COUNT = 802;
	
	private String DC_TRAIN_PATH = "train";
	private String DC_TEST_PATH = "test/zh_4g_kw_filter_group/woman1";

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
		//2、训练过程--遍历训练分类文本的文件夹，添加所有的训练分类文本
		ArrayList list = FileOperateUtils.getAllFilesPath(new File(DC_TRAIN_PATH));
		for (int i = 0; i < list.size(); i++) {
			File f = new File(list.get(i).toString());
			String className = f.getParent();
			className = className
					.substring(className.lastIndexOf("\\") + 1);
			//将训练分类文本加载到内存中
			String contentText = FileUtils.readFileToString(f, DC_ENCODING_STRING);
			DeepClassifierLibrary.Instance.DC_AddTrain(
					className, contentText);
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
		ArrayList list = FileOperateUtils.getAllFilesPath(new File(DC_TEST_PATH));
		long fLen = list.size();
		long fRLen = 0; // 总准确
		long mLen = 0; // 男性
		long mRLen = 0; // 男性准确
		long wLen = 0; // 女性
		long wRLen = 0; // 女性准确
		long iLen = 0; // 无效
		long iRLen = 0; // 无效文件
		long lMLen = 0; // 女疑男
		long lWLen = 0; // 男疑女
		long lILen = 0; // 疑无效
		for (int i = 0; i < fLen; i++) {
			// 文件名
			File f = new File(list.get(i).toString());
			String fileName = f.getName();
			boolean nRename = false;
			if (fileName.indexOf(",") > 0) {
				fileName = fileName.split(",")[1];
				nRename = true;
			}
			// 加载文件内容
			String content = FileUtils.readFileToString(f, DC_ENCODING_STRING);
			// 输出分类结果
			String classify = DeepClassifierLibrary.Instance.DC_Classify(content);
			// 判断分类准确性
			String sex = fileName.split("-")[0];
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
				if (nRename) f.renameTo(new File(f.getParent() + '\\' + fileName));
			} else {
				// 标记分类
				f.renameTo(new File(f.getParent() + "\\" + classify + "," + fileName));
				System.out.println(classify + ":" + fileName);
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
	}
	
	public static void main(String avgs[]){
		DeepClassifierTest test = new DeepClassifierTest();
		try{
//			test.didTrain();
			test.didClassify();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
