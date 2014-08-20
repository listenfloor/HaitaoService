package com.efreight.commons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * 配置文件帮助类
 * @author xianan
 *
 */
public class PropertiesUtils {

	/**
	 * 指令配置文件
	 */
	public static Properties commandProperty = new Properties();
	/**
	 * 指令配置文件
	 */
	public static Properties expressProperty = new Properties();
	
	/**
	 * 消息类型配置文件
	 */
	private static Properties msgTypeProperty = new Properties();
	/**
	 * 消息类型及结果信息配置文件
	 */
	private static Properties errorMsgTypeProperty = new Properties();
	/**
	 * 生产配置文件
	 */
	private static Properties productProperty = new Properties();
	/**
	 * wx服务器fileid配置文件
	 */
	private static Properties wxfileProperty = new Properties();
	
	private static Properties menuProperty = null;
	
	static{
		System.out.println("-------------ProperiesUtil-----------");
		InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(PropertiesUtils.class.getResource("").getPath()+"command.properties"));
			commandProperty.load(in);
			in = new BufferedInputStream(new FileInputStream(PropertiesUtils.class.getResource("").getPath()+"msgtype.properties"));
			msgTypeProperty.load(in);
			in = new BufferedInputStream(new FileInputStream(PropertiesUtils.class.getResource("").getPath()+"errormsgtype.properties"));
			errorMsgTypeProperty.load(in);
			in = new BufferedInputStream(new FileInputStream(PropertiesUtils.class.getResource("").getPath()+"product.properties"));
			productProperty.load(in);
			in = new BufferedInputStream(new FileInputStream(PropertiesUtils.class.getResource("").getPath()+"wxfile.properties"));
			wxfileProperty.load(in);
			in = new BufferedInputStream(new FileInputStream(PropertiesUtils.class.getResource("").getPath()+"expressinfo.properties"));
			expressProperty.load(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String filepath = PropertiesUtils.class.getResource("").getPath()+"command.properties";
		String s = " ssss ss ";
		String[] ss = s.split(" ");
//		String value = readValue(filepath,"help");
		System.out.println(s.trim());
	}
	
	/**
	 * 获取指令
	 * @param filePath
	 * @param key
	 * @return String
	 */
	public static String readValue(String filePath, String key) {
		try {
			String value = commandProperty.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取消息类型
	 * @param filePath
	 * @param key
	 * @return String
	 */
	public static String readMsgTypeValue(String filePath, String key) {
		try {
			String value = msgTypeProperty.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取消息类型及结果信息
	 * @param filePath
	 * @param key
	 * @return String
	 */
	public static String readERRORMsgTypeValue(String filePath, String key) {
		try {
			String value = errorMsgTypeProperty.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取生产/测试配置信息
	 * @param filePath
	 * @param key
	 * @return String
	 */
	public static String readProductValue(String filePath, String key) {
		try {
			String value = productProperty.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取生产/测试微信服务器上fileid配置信息
	 * @param filePath
	 * @param key
	 * @return String
	 */
	public static String readWXFileValue(String filePath, String key) {
		try {
			String value = wxfileProperty.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String readCustomMenuSettingValue(String key) {
		if(menuProperty == null){
			menuProperty = new Properties();
			LoadCustomMenuProperty();
		}
		File refresh = new File(PropertiesUtils.class.getResource("").getPath(),"refresh");
		if(refresh.exists()){
			LoadCustomMenuProperty();
			refresh.deleteOnExit();
		}
		return menuProperty.getProperty(key);
			
	}

	private static void LoadCustomMenuProperty() {
		try {
			menuProperty.load(new BufferedInputStream(new FileInputStream(PropertiesUtils.class.getResource("").getPath()+"customMenu.properties")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
