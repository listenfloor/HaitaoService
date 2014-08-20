package com.efreight.weixin.handler;

import org.dom4j.Document;

import com.efreight.weixin.WXMessageLogHelper;

/**
 * 处理用户发送过来声音消息的请求，继承WXMessageHandler类
 * @author xianan
 *
 */
public class VOICEMessage extends WXMessageHandler{

	/**
	 * 唯一构造方法
	 * @param doc 微信请求过来的xml转换成dom4j的Document类型。
	 * @param url 
	 */
	public VOICEMessage(Document doc,String url){
		this.doc = doc;
		this.url=url;
	}
	
	
	/**
	 * 目前未能找到音频文件在微信服务器上对应关系，所以无法保存至本地。
	 */
	public String process(){
		//发送日志，voice 为声音类型
		new Thread(new WXMessageLogHelper(this.doc, false, "true","VOICE")).start();//保存日志
		return null;
	}
	
}
