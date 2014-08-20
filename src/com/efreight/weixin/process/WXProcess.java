package com.efreight.weixin.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXMessageLogHelper;

/**
 * 抽象类，处理文本消息。方法分为同步和异步处理。
 * @author xianan
 *
 */
public abstract class WXProcess {

	/**
	 * dom4j的Document。
	 */
	protected Document doc;
	
	protected int timerMil=3500;
//	protected final String WXSystemCode="freight_in_hand";
	/**
	 * wx服务器标示。
	 */
	protected final String WXSystemCode=PropertiesUtils.readProductValue(null, "username");
	/**
	 * 用户的openid，是用户于平台关联唯一id。来源用户请求。
	 */
	protected String openId;//openid
	/**
	 * 消息体
	 */
	protected String message;//消息体
	/**
	 * 微信系统唯一消息标示。
	 */
	protected String wxMsgId;//WX消息ID
	/**
	 * 错误信息。
	 */
	protected String errorMsgType="";//错误消息类型
	/**
	 * 消息类型，为日志统计用
	 */
	protected String msgType="";//消息类型
	/**
	 * 转换后消息。
	 */
	protected String clearMessage = "";
	public String command = "";
	
	//同步处理
	/**
	 * 同步处理
	 * @return String
	 */
	public abstract String snycProcess();
	//异步处理
	/**
	 * 异步处理
	 */
	public abstract void asnycProcess();
	public void setClearMessage(String clearMessage) {
		this.clearMessage = clearMessage;
	}
	/**
	 * 处理错误消息
	 * 给用户发送帮助消息
	 */
	public String errorProcess(){
		//开始定时器
			List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
			Map<String,String> guide = new HashMap<String, String>();
			guide.put("title",WeixinI18nUtil.getMessageWithOpenid(openId, "help_title", null));
			guide.put("content", WeixinI18nUtil.getMessageWithOpenid(openId, "help_title", null));
			guide.put("picUrl","http://m.eft.cn/helpdoc/images/plane.jpg");
			guide.put("url", WeixinI18nUtil.getMessageWithOpenid(openId, "help_url", null));
			messageList.add(guide);
			String responseXml = WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId, messageList,this.wxMsgId,"UNKNOW","NORMAL");
			return responseXml;
	}
}
