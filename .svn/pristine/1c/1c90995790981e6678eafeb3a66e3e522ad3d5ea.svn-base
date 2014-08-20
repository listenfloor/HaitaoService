package com.efreight.commons;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONObject;

import com.efreight.weixin.WXInfoDownloader;

public class WeixinI18nUtil {
	private Locale locale = null;
	private static final String BUNDLEBASENAME = "com.efreight.weixin.i18n.weixin";
	private static Map<String,String> userinfo = new HashMap<String,String>();
	public WeixinI18nUtil(Locale locale) {
		this.locale = locale;
	}
	
	public ResourceBundle getBundle() {
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle(BUNDLEBASENAME, this.locale);
		} catch (Exception e) {
			Locale l = new Locale("en");
			bundle = ResourceBundle.getBundle(BUNDLEBASENAME,l);
		}
		
		return bundle;
	}
	
	public String getMessage(String msgName,Object[] args) {
		ResourceBundle bundle = this.getBundle();
		String msg = bundle.getString(msgName);
		
		return MessageFormat.format(msg, args);
	}
	 
	public static String getMessageWithOpenid(String openid,String msgName,Object[] args){
		String message = "";
		try {
			
			WXInfoDownloader d = new WXInfoDownloader();
			JSONObject userinfo = d.getUserInfoJSON(openid);
			String language = "zh_cn";
			try{
				language = userinfo.getString("language");
			}catch(Exception e) {
				System.out.println("获取客户端语言失败，使用默认语言" + language);
			}
				//WeixinI18nUtil.userinfo.put(openid, language);
			Locale locale = new Locale(language);
			WeixinI18nUtil util = new WeixinI18nUtil(locale);
			message = util.getMessage(msgName, args);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return message;
	}
	
}
