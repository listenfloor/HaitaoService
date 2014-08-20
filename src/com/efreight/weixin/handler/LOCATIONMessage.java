package com.efreight.weixin.handler;

import org.dom4j.Document;

import com.efreight.weixin.WXMessageLogHelper;

/**
 * 处理用户发送过来位置信息的请求，继承WXMessageHandler类
 * @author xianan
 *
 */
public class LOCATIONMessage extends WXMessageHandler {
	

	/**
	 * 唯一构造方法
	 * @param doc 微信请求过来的xml转换成dom4j的Document类型。
	 * @param url 
	 */
	public LOCATIONMessage(Document doc,String url){
		this.doc = doc;
		this.url=url;
	}
	
	
	/**
	 * 纪录用户提交信息
	 */
	public String process() {
		//发送日志，location为地标类型
		new Thread(new WXMessageLogHelper(doc, false, "true","LOCATION")).start();//保存日志
		return null;
	}

}
