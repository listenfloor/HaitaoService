package com.efreight.weixin.handler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Node;

import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.process.HELPProcess;
import com.efreight.weixin.process.WXProcess;
import com.efreight.weixin.process.WXProcessHandler;
import com.sun.xml.internal.fastinfoset.sax.Properties;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理用户发送过来的文本消息，继承WXMessageHandler类
 * @author xianan
 *
 */
public class TEXTMessage extends WXMessageHandler {
	/**
	 * code属性，判断用户发送指令后给处理完成后的消息。（运单使用）
	 */
	private String code = "";

	/**
	 * 唯一构造方法
	 * @param doc 微信请求过来的xml转换成dom4j的Document类型。
	 * @param url 
	 */
	public TEXTMessage(Document doc, String url) {
		this.doc = doc;
		this.url = url;
	}

	/**
	 * 流程为：
	 * 1调用checkTextMessageSynOrAsyn方法，判断指令。
	 * 2根据指令调用不同类的不同方法。
	 * 3如果返回指令不明确，则认为非指令消息，回复帮助文档。
	 * @return String
	 */
	public String process() {
//		long timeStart = System.currentTimeMillis();
		System.out.println("!!!===1");
		try {
			this.response.setContentType("text/html; charset=utf-8");
			this.response.setCharacterEncoding("UTF-8");
			this.response.getOutputStream().write(" ".getBytes("UTF-8"));
			this.response.getOutputStream().flush();
			this.response.getOutputStream().close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String message = "";
		String messagetype = doc.selectSingleNode("//MsgType").getText();
		String openid= doc.selectSingleNode("//FromUserName").getText();
		if(messagetype.equalsIgnoreCase("voice")){
			message = doc.selectSingleNode("//Recognition").getText();
			WXInfoDownloader util = new WXInfoDownloader();
			
			try {
				util.SendWXTextMessageWithCustomAPI(openid, "您的语音信息解析为：" + message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			message = doc.selectSingleNode("//Content").getText();
		try{
			message=message.trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("!!!===2");
		String responseXML = null;
		// 先通过配置文件判断是同步还是异步
		List<String> clearMessage = new ArrayList<String>(); 
		String result = this.checkTextMessageSynOrAsyn(message,clearMessage);// 判断分析具体应发送至某类
		System.out.println("!!!===3" + result);
		if (result != null && !"".equals(result) && result.indexOf('|') > 0) {
			String[] cm = result.split("\\|");
			Class<WXProcess> target = null;
			try {
				target = (Class<WXProcess>) Class
						.forName("com.efreight.weixin.process."
								+ cm[0].toUpperCase() + "Process");
				Constructor<?> con = target.getConstructor(Document.class);
				WXProcess handler = (WXProcess) con.newInstance(doc);
				if(clearMessage!=null&&clearMessage.size()>0){
					if(clearMessage.get(0)!=null&&!"".equals(clearMessage.get(0))){
						handler.setClearMessage(clearMessage.get(0));
					}
				}
				handler.command = result;
				if (cm[1].equals("snycProcess")) {// 同步消息
					
					responseXML = handler.snycProcess();
				} else {// 异步消息
					handler.asnycProcess();
				}
			} catch (Exception e) {
				System.out.println("create Handler error.");
//				new Thread(new WXMessageLogHelper(doc, false, true, "UNKNOW")).start();// 保存日志未知消息类型
				e.printStackTrace();
			}
			//这里保存日至移到内部
		}else{
			new Thread(new WXMessageLogHelper(doc, false, "true", "UNKNOW")).start();// 保存日志未知消息类型
			HELPProcess helpProcess = new HELPProcess(doc);
			helpProcess.setErrorMsgType("NORMAL");
			helpProcess.setMsgType("UNKNOW");
//			responseXML = helpProcess.errorProcess();
		}
		System.out.println("*************************");
		System.out.println(responseXML);
		if(responseXML != null && !"".equals(responseXML)){
			WXAPIServiceProcess process = new WXAPIServiceProcess();
			process.process(responseXML);
		}
		return "";
	}

	/**
	 * 首先先进行消息转换。把中文数字转换成阿拉伯数字。
	 * 判断消息指令是否由空格拆分。空格前为指令。指令在com.efreight.commons下面的command.properties（目前一共有tact help sub y m 订阅 帮助 这几种指令。）
	 * 如果没有指令，则判断消息体是否是11位数字，如果是11位数字，则判断为运单/订单查询。
	 * @param message 微信用户上传的消息体
	 * @param clearMessage 经过转换过后的指令。
	 * @return 返回处理指令。格式为xxxx|yyyyyy|zzzz   x代表转给某个类去处理。 y代表x的y方法处理。z代表属于某种日至级别。     
	 */
	private String checkTextMessageSynOrAsyn(String message, List<String> clearMessage) {// 解析指令，返回String
																// 格式为
																// className|methodName
		String msg = message.toUpperCase();
		Enumeration<Object> keys = PropertiesUtils.commandProperty.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if(msg.startsWith(key)) {
				return PropertiesUtils.readValue("", key);
			}
				
		}

		Pattern shippingReg = Pattern.compile("^.[a-zA-Z]{3,}");
		Matcher shippingMatcher = shippingReg.matcher(msg);
		if(shippingMatcher.find()) {
			return "SHIPPING|snycProcess|SHIPPING";
		}
		
		String returnMessage = "";
		returnMessage = this.expressInfo(msg);
		if(returnMessage != null && !"".equals(returnMessage)) {
			return returnMessage;
		}
		code = message.replaceAll("一", "1").replaceAll("壹", "1")
				.replaceAll("二", "2").replaceAll("贰", "2").replaceAll("三", "3")
				.replaceAll("叁", "3").replaceAll("四", "4").replaceAll("肆", "4")
				.replaceAll("五", "5").replaceAll("伍", "5").replaceAll("六", "6")
				.replaceAll("陆", "6").replaceAll("七", "7").replaceAll("柒", "7")
				.replaceAll("八", "8").replaceAll("捌", "8").replaceAll("九", "9")
				.replaceAll("玖", "9").replaceAll("零", "0");
		Pattern regex = Pattern.compile("\\D");
		Matcher matcher = regex.matcher(code);
		code = matcher.replaceAll("");
		message = message.trim();
		Pattern p = Pattern.compile("\\d{11}");  
		Matcher m = p.matcher(code);  
		if (message != null) {
//			String key = null;
//			if (message.indexOf(" ") > 0) {// 说明发送指令
//				key = message.substring(0, message.indexOf(" ")).toUpperCase();
//			} else {
//				key = message.toUpperCase();
//			}
//			returnMessage = PropertiesUtils.readValue("", key);
//			if (returnMessage != null && !"".equals(returnMessage)) {
//				return returnMessage;
//			}
			if (m.matches()) {// 这里判断11位运单号，或者其他 格式为：xxx-xxxxxxxx或者为xxxxxxxxxxx
				returnMessage = "TRACE|asnycProcess|TRACE";
				clearMessage.add(code);
			} else {
				returnMessage = "";
			}
			System.out.println("start airport");
			p = Pattern.compile("^[a-zA-Z]{3}$"); 
			m = p.matcher(message);  
			if(m.matches()){// 这里判断是否是港口
				System.out.println("airport is true");
				returnMessage = "AIRPROTANDCITY|snycProcess|TACT";
			}
		}
		return returnMessage;
	}

	private static String expressInfo(String msg) {
		Pattern regex = Pattern.compile("[\u4E00-\u9FA5]*");
		Matcher matcher = regex.matcher(msg);
		String m = "";
		String expkey = "";
		if (matcher.find()) {
			m = matcher.group(0);
			if(!m.equals("")){
				expkey = msg.substring(msg.indexOf(m)+m.length());
			}else {
				int size = 3;
				if(msg.toLowerCase().contains("fedex")){
					size=5;
				}else if (msg.toLowerCase().contains("usps")) {
					size =4;
				}
			
					m = msg.substring(0,size);
					expkey = msg.substring(size);
			}
		}
		m = m.replaceAll("快递|物流|货运|快件|速递|邮政|速运|快运", "");
		if (m.length() < 2)
			return null;
		Enumeration<Object> keys = PropertiesUtils.expressProperty.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.toLowerCase().contains(m.toLowerCase()))
				return "EXPRESS|snycProcess|EXPRESS|"
						+ PropertiesUtils.expressProperty.getProperty(key)
						+ "|" + key + "|"
						+ expkey;
		}
	
		return null;
	}
	public static void main(String[] args) {
//		String s = "dhl394720348";
//		System.out.println(expressInfo(s));

		
	}
}
