package com.cdc.deepClassify;

public class ClassifySexAge {
	private String[] classify = {
			"男,0,14",
			"女,0,14",
			"男,15,21",
			"女,15,21",
			"男,22,25",
			"女,22,25",
			"男,26,40",
			"女,26,40",
			"男,41,60",
			"女,41,60",
			"男,61,120",
			"女,61,120"
	};
	private String[] keyword;
	public ClassifySexAge(){
		keyword = new String[50];
		for(int i = 0; i < keyword.length; i++){
			keyword[i] = "";
		}
	}
	 
	public void add(String sex, int age, String kw){
		int sexI = 0;
		int ageI = age - 10;
		if (sex.equals("男")) {
			sexI = 2;
		} else if (sex.equals("女")) {
			sexI = 1;
		} else {
			return;
		}
		
//		if (age <= 14) ageI = 1;
//		if (age <= 21) ageI = 2;
//		if (age <= 25) ageI = 3;
//		if (age <= 40) ageI = 4;
//		if (age <= 60) ageI = 5;
//		if (age <= 120) ageI = 6;
		
		int classify = ageI * 2 - sexI;
		keyword[classify] += kw + "\n";
	}
	public String[] getKeyword(){
		return keyword;
	}
}
