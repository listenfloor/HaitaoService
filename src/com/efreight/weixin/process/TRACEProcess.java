package com.efreight.weixin.process;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;

/**
 * 处理运单和订单查询请求。继承WXProcess类
 * @author xianan
 *
 */
public class TRACEProcess extends WXProcess {
	
	private static Map<String,String> LastPushAwbCode = new HashMap<String, String>();
	
	public synchronized static void updateLastPushAwbCode(String openid,String awbcode) {
		System.out.println("添加详细回复的运单号：" + awbcode);
		LastPushAwbCode.put(openid, awbcode);
	}
	/**
	 * 构造方法
	 * @param doc
	 */
	public TRACEProcess(Document doc){
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		String messagetype = doc.selectSingleNode("//MsgType").getText();
		if(messagetype.equalsIgnoreCase("voice"))
			this.message = doc.selectSingleNode("//Recognition").getText();
		else
			this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}
	
	public TRACEProcess() {
		
	}
	public String snycProcess() {
		if(this.clearMessage!=null&&!"".equals(this.clearMessage)){
			this.message = this.clearMessage;
		}
		String wxfakeid = null;
		long timeStart = System.currentTimeMillis();
		try {
			wxfakeid = WXInfoDownloader.userWithOpenId.get(openId).fakeid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("snycProcess");
		String responseXml = "";
		if (!message.startsWith("010")) {
			new Thread(new WXMessageLogHelper(doc, false, "true", "AWBTRACE")).start();// 保存日志
			//判断运单
			try{
				int order =  Integer.parseInt(message.substring(3, 10));
				int lastNumber = Integer.parseInt(message.substring(10));
				if(order%7!=lastNumber){
					throw new Exception();
				}
			}catch(Exception e){
				responseXml =  WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "运单格式错误，请检查");
				try {
					Document respDoc = DocumentHelper.parseText(responseXml);
					new Thread(new WXMessageLogHelper(respDoc, true, "true", "AWBTRACE","FORMATERROR",this.wxMsgId)).start();// 保存日志
				} catch (DocumentException e1) {
					e1.printStackTrace();
				}
				return responseXml;
			}
			try {
				WXProcessHandler.getAircompanyList();
				String airCompanyName = WXProcessHandler.aircompanyList.get(message.substring(0, 3));
				if (airCompanyName == null) {// 不支持的航空公司
					responseXml =  WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "aircompany_notsupport", null));
					try {
						Document respDoc = DocumentHelper.parseText(responseXml);
						new Thread(new WXMessageLogHelper(respDoc, true, "true", "AWBTRACE","UNSUPPORT",this.wxMsgId)).start();// 保存日志
					} catch (DocumentException e1) {
						e1.printStackTrace();
					}
					return responseXml;
				} else{
					String subscribertype = "WEIXIN";// subscribe ? "WEIXIN" : "NONE";
					String limit_num = "1";
					String sync = "Y";
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String requestXml = "<eFreightService>"
							+ "<ServiceURL>Subscribe</ServiceURL>"
							+ "<ServiceAction>TRANSACTION</ServiceAction>"
							+ "<ServiceData>" + "<Subscribe>"
							+ "<type>trace</type><target>" + message.substring(0, 3) + '-' + message.substring(3)
							+ "</target><targettype>MAWB</targettype>" + "<sync>" + sync
							+ "</sync><subscriber>" + wxfakeid + "</subscriber><wxMsgId>"
							+ wxMsgId + "</wxMsgId><subscribertype>" + subscribertype
							+ "</subscribertype>"
							+ "<standard_type>3</standard_type><limit_num>" + limit_num
							+ "</limit_num><offflag></offflag><systime>"+format.format(new Date())+"</systime></Subscribe>" + "</ServiceData>"
							+ "</eFreightService>";
					String responseData  = "";
					try{
					responseData = HttpHandler
							.postHttpRequest(
									PropertiesUtils.readProductValue("", "awbtraceurl"),
									requestXml);
					}catch(Exception e){
						responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "轨迹查询超时");
						try {
							Document respDoc = DocumentHelper.parseText(responseXml);
							new Thread(new WXMessageLogHelper(respDoc, true, "true", "AWBTRACE","TIMEOUT",this.wxMsgId)).start();// 保存日志
						} catch (DocumentException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return responseXml;
					}
					long timeRecive = System.currentTimeMillis();
					System.out.println("接收时间:"+(timeRecive-timeStart));
					System.out.println(responseData);
					if(responseData==null||"".equals(responseData)){
						responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "轨迹查询出错");
						try {
							Document respDoc = DocumentHelper.parseText(responseXml);
							new Thread(new WXMessageLogHelper(respDoc, true, "true", "AWBTRACE","ERROR",this.wxMsgId)).start();// 保存日志
						} catch (DocumentException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return responseXml;
					}
					Document doc = DocumentHelper.parseText(responseData);
					Node errorMsg = doc.selectSingleNode("//isError");
					if(errorMsg!=null&&errorMsg.getText()!=null&&"1".equals(errorMsg.getText())){
						responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "轨迹查询出错");
						try {
							Document respDoc = DocumentHelper.parseText(responseXml);
							new Thread(new WXMessageLogHelper(respDoc, true, "true", "AWBTRACE","ERROR",this.wxMsgId)).start();// 保存日志
						} catch (DocumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return responseXml;
					}
					List<Node> tracesNodeList = doc.selectNodes("//TraceTranslate");
					if(tracesNodeList!=null&&tracesNodeList.size()>0){
						responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "点击查看运单<a href=\""
							+ PropertiesUtils
									.readProductValue("", "waybillurl")
							+ "?mawbcode=" + message.substring(0, 3) + '-' + message.substring(3) + "p" + wxfakeid + "p" + wxMsgId
							+ "\">" + message.substring(0, 3) + '-' + message.substring(3) + "</a>最新轨迹");
						try {
							Document respDoc = DocumentHelper.parseText(responseXml);
							new Thread(new WXMessageLogHelper(respDoc, true, "true", "AWBTRACE","NORMAL",this.wxMsgId)).start();// 保存日志
						} catch (DocumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						long timeEnd = System.currentTimeMillis();
						System.out.println("总耗时:"+(timeEnd-timeStart));
						return responseXml;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
//			try {
//				new Thread(new WXMessageLogHelper(doc, false, "true", "MARKETTRACE")).start();// 保存日志
//				data = WXProcessHandler.GetTraceData(message.substring(0, 3) + '-' + message.substring(3, 7) + "-"
//						+ message.substring(7), openId, wxfakeid,this.wxMsgId);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		return responseXml;
	}


	/**
	 * 异步方法
	 * 根据整理后的消息体（去除字符后为11位订单/运单号）
	 * 1判断是否为010开头。如果是010开头则是订单，非010开头的是其他航空公司运单。
	 * 2如果是非010开头的，首先验证运单格式。规则为第4位开始，到第10位数字％7 的余数是否等于最后一位验证位。如果相等，则验证通过，如果不等则返回运单格式错误的消息。
	 * 3通过WXProcessHandler的GetAwbTraceData方法发送运单查询请求。
	 * 4返回结果中判断是否需要发送订阅提醒消息。
	 * 5如果是订单，则通过WXProcessHandler的GetTraceData方法，发送订单。订单查询不发送订阅提醒
	
	*/
	public void asnycProcess() {
		String data = "";
		String wxfakeid = null;
		try {
			wxfakeid = WXInfoDownloader.userWithOpenId.get(openId).fakeid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(wxfakeid==null||wxfakeid.equals("")){
			System.out.println("fakeid 没有查到");
			return;
		}
		if(message.equals("详细")){
			
			String code = null;
			if((code = LastPushAwbCode.get(openId)) != null) {
				message = code.length() == 12?code.replace("-",""):message;
				
			}else {
				new Thread(new WXMessageLogHelper(doc, false, "true", "AWBTRACE")).start();// 保存日志
				data = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "sub_error", null), openId, this.wxMsgId, "AWBTRACE",
						"DETAILERROR");
				WXAPIServiceProcess service = new WXAPIServiceProcess();
				boolean success =service.process(data).contains("发送成功");
				return;
			}
		}
			
		
		if(this.clearMessage!=null&&!"".equals(this.clearMessage)){
			this.message = this.clearMessage;
		}
		//this.getclearCode();
		boolean awbSubTip = false;
		if (!message.startsWith("010")) {
			new Thread(new WXMessageLogHelper(doc, false, "true", "AWBTRACE")).start();// 保存日志
			//判断运单
			try {
				int order = Integer.parseInt(message.substring(3, 10));
				int lastNumber = Integer.parseInt(message.substring(10));
				if (order % 7 != lastNumber) {
					throw new Exception();
				}
			} catch (Exception e) {
				data = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "mawb_format_error", null), openId, this.wxMsgId, "AWBTRACE",
						"FORMATERROR");
			}
			if(data==null||"".equals(data)){
				try {
					WXProcessHandler.getAircompanyList();
					data = WXProcessHandler.GetAwbTraceData(message.substring(0, 3) + '-' + message.substring(3), wxfakeid, openId,this.wxMsgId,"AWBTRACE");
				} catch (Exception e) {
					e.printStackTrace();
				}

				data = data.replace("SendWXImageAndTextMessage", "SendWXImageAndTextMessageWithCustomAPI");
			}
		} else {
			try {
				new Thread(new WXMessageLogHelper(doc, false, "true", "MARKETTRACE")).start();// 保存日志
				data = WXProcessHandler.GetTraceData(message.substring(0, 3) + '-' + message.substring(3, 7) + "-"
						+ message.substring(7), openId, wxfakeid,this.wxMsgId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(data!=null&&!"".equals(data)){
			System.out.println("!!!=====4" + data);
			WXAPIServiceProcess service = new WXAPIServiceProcess();
			boolean success =service.process(data).contains("发送成功");
		}
//		if (awbSubTip) {
//			AddCommand(wxfakeid);
//		}
		//增加订阅
		this.AddCommand(message,this.openId,wxfakeid);
	}
	
	
	/**
	 * 增加订阅提醒处理。
	 * 首先发送订阅提醒消息。如果发送成功再把订单相关信息保存到WXProcessHandler.commandChains中。
	 * @param awbcode
	 * @param openId
	 * @param wxfakeid
	 */
	public void AddCommand(String awbcode, String openId, String wxfakeid) {
		Map<String, Object> detailData = new HashMap<String, Object>();
		detailData.put("datetime", new Date().getTime() + 300000);
		detailData.put("command", "subscribe");
		detailData.put("key", awbcode.replace("-", ""));
		WXProcessHandler.commandChains.put("Y" + openId, detailData);
	}

}
