package com.cdc.deepClassify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ClassifyData {
	private int partCount;
	private HashMap<String, String[]> classify = new HashMap<String, String[]>();
	private HashMap<String, Integer> partIndex = new HashMap<String, Integer>();
	
	public ClassifyData(){
		this(100);
	}
	
	public ClassifyData(int partCount){
		this.partCount = partCount;
	}
	
	public void put(String label, String value){
		// 创建新的类别
		if (!classify.containsKey(label)){
			classify.put(label, new String[partCount]);
			partIndex.put(label, 0);
		}
		// 放入值
		String labelPart[] = classify.get(label);
		int pIndex = partIndex.get(label);
		if (labelPart[pIndex] == null) labelPart[pIndex] = "";
		labelPart[pIndex] += value + "\n";
		classify.put(label, labelPart);
		partIndex.put(label, ++pIndex % partCount);
	}
	
	public Set<String> getLabels(){
		return classify.keySet();
	}
	
	public int getPartCount(){
		return partCount;
	}
	
	public String[] getValues(String label) {
		return classify.get(label);
	}
	
	public HashMap<String, String[]> get(){
		return classify;
	}
}
