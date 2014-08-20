package com.efreight.weixin.process;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.WXUserinfo;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 处理取消订阅消息，WXProcess类
 * @author xianan
 *
 */
public class USUBProcess extends WXProcess {
	// key:openid,value{key:Y+openid,value:{时间戳，订阅标示，运单号}}订阅用

	/**
	 * 构造方法
	 * @param doc
	 */
	public USUBProcess(Document doc) {
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}

	public String snycProcess() {
		String responseXml = "";
		//首先判断是否是回复订阅消息
		message = message.trim();
		new Thread(new WXMessageLogHelper(doc, false, "true", "USUBSCRIBE")).start();// 保存日志
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wxfakeid==null||wxfakeid.equals("")){
			System.out.println("fakeid 没有查到");
			return "";
		}
		message = message.toUpperCase().replaceAll(" ", "").replaceAll("-", "").replaceAll("USUB", "").replaceAll("取消", "").replaceAll("订阅", "").trim();
		Pattern p = Pattern.compile("\\d{11}");  
		Matcher m = p.matcher(message);  
		if(m.matches()){
			try {
				WXProcessHandler.USubAwbTraceData(message.substring(0, 3) + '-' + message.substring(3), wxfakeid, false, this.openId, this.wxMsgId, "USUBSCRIBE");
				responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,  "运单" + message + "已成功取消订阅。", openId, this.wxMsgId, "USUBSCRIBE","NORMAL");
//				responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "运单" + message + "已成功取消订阅。");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,  "输入的运单号有误。", openId, this.wxMsgId, "USUBSCRIBE","NORMAL");
//			responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "订阅运单号有误。");
		}
		return responseXml;
	}

	/**
	 * 异步处理。
	 * 首先判断消息体是否是y或者m（用于对前一次查询运单后的订阅）。y为微信消息订阅，m为邮件订阅。
	 * 判断消息中是否之前查过订单，如果查过，则通过SubscribeAwb方法订阅之前的消息。
	 * 如果不是y或m则判断是否是sub或者'订阅'指令 ＋ 11位运单号，通过SubscribeAwb方法进行对某运单的订阅。如果不是则返回错误的帮助消息。
	 */
	public void asnycProcess() {
		//首先判断是否是回复订阅消息
		message = message.trim();
		String commandForChains = "";
		
		new Thread(new WXMessageLogHelper(doc, false, "true", "USUBSCRIBE")).start();// 保存日志
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wxfakeid==null||wxfakeid.equals("")){
			System.out.println("fakeid 没有查到");
			return;
		}
		WXUserinfo user = null;
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		try{
			user = WXInfoDownloader.userWithOpenId.get(openId);
		}catch(Exception e){
			try {
				user = (WXUserinfo)sqlMap.queryForObject("getwxuserinfobyopenid",openId);
				WXInfoDownloader.userWithOpenId.put(openId, user);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		WXProcessHandler.getAircompanyList();
		Map<String, Object> command = WXProcessHandler.commandChains.get(commandForChains);
		message = message.toUpperCase().replaceAll(" ", "").replaceAll("-", "").replaceAll("USUB", "").replaceAll("取消", "").replaceAll("订阅", "").trim();
		Pattern p = Pattern.compile("\\d{11}");  
		Matcher m = p.matcher(message);  
		if(m.matches()){
			try {
				this.USubscribeAwb(openId, message.substring(0, 3) + '-' + message.substring(3), wxfakeid);
			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "订阅运单号有误", openId,this.wxMsgId,"USUBSCRIBE","USUBFAIL");
			WXAPIServiceProcess service = new WXAPIServiceProcess();
			boolean success = service.process(responseXml).contains("发送成功");
		}
	}

	/**
	 * 订阅
	 * 
	 * @param openid 用户和平台在微信的唯一对应关系
	 * @param message 订单号
	 * @param wxfakeid 用户唯一id
	 * @throws Exception
	 * @throws DocumentException
	 */
	private void USubscribeAwb(String openid, String message, String wxfakeid) throws Exception, DocumentException {
		String responseXml = "";
		if (message.length() == 12) {
			WXProcessHandler.GetAwbTraceData(message, wxfakeid, false, openid, this.wxMsgId, "USUBSCRIBE",null);
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "运单" + message + "已成功取消订阅。", openid,
					this.wxMsgId, "SUBSCRIBE", "SUCCESS");
		} else {
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "取消订阅失败，您输入的运单号不是有效的运单！", openid, this.wxMsgId,
					"SUBSCRIBE", "SUBFAIL");
		}
		WXAPIServiceProcess service = new WXAPIServiceProcess();
		boolean success = service.process(responseXml).contains("发送成功");
	}

}
