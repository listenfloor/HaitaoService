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
import com.efreight.weixin.WXAPIService;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXMessageLogHelper;

/**
 * 处理help消息，继承WXProcess类。
 * @author xianan
 *
 */
public class HELPProcess extends WXProcess {
	/**
	 * 用户唯一标示。
	 */
	private String wxfakeid;
	/**
	 * 构造方法。
	 * @param doc
	 */
	public HELPProcess(Document doc){
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
//		this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}
	/**
	 * 同步处理
	 */
	public String snycProcess() {
		new Thread(new WXMessageLogHelper(doc, false, "true", "HELP")).start();// 保存日志
		List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
		Map<String,String> guide = new HashMap<String, String>();
		guide.put("title",WeixinI18nUtil.getMessageWithOpenid(openId, "help_title", null));
		guide.put("content", WeixinI18nUtil.getMessageWithOpenid(openId, "help_title", null));
		guide.put("picUrl","http://m.eft.cn/helpdoc/images/plane.jpg");
		guide.put("url", WeixinI18nUtil.getMessageWithOpenid(openId, "help_url", null));
		messageList.add(guide);
		//判断定时器
		String responseXml = WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId, messageList,this.wxMsgId,"HELP","NORMAL");
		
		return responseXml;
	}

	/**
	 * 异步处理
	 * 首先通过WXProcessHandler中的GetWXFakeidWithOpenid方法查找到用户的唯一表示。
	 * 然后再向用户推送帮助消息。
	 */
	public void asnycProcess() {
		wxfakeid=null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(wxfakeid==null||"".equals(wxfakeid))
			return;
		new Thread(new WXMessageLogHelper(doc, false, "true", "HELP")).start();// 保存日志
		String responsexml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>SendWXImageAndTextMessageWithID</ServiceAction><ServiceData><msgid>"+PropertiesUtils.readProductValue("", "helpmsgid")+"</msgid><wxfakeid>"
				+ wxfakeid + "</wxfakeid><openid>" + openId + "</openid><wxMsgId>"+wxMsgId+"</wxMsgId><MsgType>HELP</MsgType></ServiceData></eFreightService>";
		WXAPIServiceProcess service = new WXAPIServiceProcess();
		service.process(responsexml);
		
//		boolean success = respXML.contains("发送成功");
//		try {
//			new Thread(new WXMessageLogHelper(DocumentHelper.parseText(responsexml), true, success)).start();
//		} catch (DocumentException e) {
//			e.printStackTrace();
//		}
	}
	
	public void setMsgType(String msgType){
		this.msgType = msgType;
	}
	
	public void setErrorMsgType(String errorMsgType){
		this.errorMsgType = errorMsgType;
	}

}
