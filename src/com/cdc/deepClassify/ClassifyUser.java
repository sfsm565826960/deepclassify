package com.cdc.deepClassify;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassifyUser {
	private static ClassifyUser user = null;
	
	public String msisdn, sex, age, value = "";
	public HashMap<String,String> labels = new HashMap<String,String>();
	
	
	public ClassifyUser(String msisdn, String sex, String age) {
		this(msisdn, sex, age, "");
	}
	
	public ClassifyUser(String msisdn, String sex, String age, String value){
		this.msisdn = msisdn;
		this.sex = sex;
		this.age = age;
		this.value = value.trim();
		this.labels.clear();
		this.labels.put("sex", sex);
		int iAge = Integer.parseInt(age);
		if (iAge < 14) {
			this.labels.put("age" ,"小孩(14以下)");
		} else if (iAge < 21) {
			this.labels.put("age" ,"中学生(15-21)");
		} else if (iAge < 25) {
			this.labels.put("age" ,"大学生(22-25)");
		} else if (iAge < 40) {
			this.labels.put("age" ,"青年(26-40)");
		} else if (iAge < 60) {
			this.labels.put("age" ,"中年(41-60)");
		} else if (iAge < 120) {
			this.labels.put("age" ,"老年(61以上)");
		} else {
			this.labels.put("age" ,"无效");
		}
	}
	
	public void addValue(String value) {
		if (this.value == null) {
			this.value = value.trim();
		} else {
			this.value += "\n" + value.trim();
		}
	}
	
	public static ClassifyUser isNewUser(String _msisdn, String _sex, String _age, String _value){
		if (null == user) {
			user = new ClassifyUser(_msisdn, _sex, _age, _value);
			return null;
		} else if (_msisdn.equals(user.msisdn)) {
			user.addValue(_value);
			return null;
		} else if (user.value.length() == 0) { // 忽略无value的用户
			user = new ClassifyUser(_msisdn, _sex, _age, _value);
			return null;
		} else {
			ClassifyUser old = user;
			user = new ClassifyUser(_msisdn, _sex, _age, _value);
			return old;
		}
	}
	
	public static ClassifyUser getUser(){
		return user;
	}
}
