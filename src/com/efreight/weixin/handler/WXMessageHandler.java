package com.efreight.weixin.handler;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;

/**
 * 抽象类，所有的消息处理类的父类。其中包括两个属性和一个方法。
 * @author xianan
 *
 */
public abstract class WXMessageHandler {

	/**
	 * Document属性，dom4j的Document属性。子类实力化的时候是需要把微信服务器发过来的xml转换成dom4j的Document的。
	 *
	 */
	protected Document doc ;
	protected String url;
	public HttpServletResponse response;
	
	/**
	 * 处理请求，返回String类型的结果。
	 * @return String
	 */
	public abstract String process();
}
