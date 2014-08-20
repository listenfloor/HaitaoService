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
import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.UserAWBHistoryServlet;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.WXUserinfo;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 处理订阅消息，WXProcess类
 * 
 * @author xianan
 * 
 */
public class SUBProcess extends WXProcess {
	// key:openid,value{key:Y+openid,value:{时间戳，订阅标示，运单号}}订阅用

	/**
	 * 构造方法
	 * 
	 * @param doc
	 */
	public SUBProcess(Document doc) {
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		String messagetype = doc.selectSingleNode("//MsgType").getText();
		if(messagetype.equalsIgnoreCase("voice"))
			this.message = doc.selectSingleNode("//Recognition").getText();
		else
			this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}

	public String snycProcess() {
		// 首先判断是否是回复订阅消息
		String errorMsgType = "SUCCESS";
		String i18nMessage = "";
		String responseXml = null;
		message = message.trim();
		String commandForChains = "";
		if (message.toUpperCase().startsWith("Y")
				|| message.toUpperCase().startsWith("M"))
			commandForChains = "Y" + openId;
		new Thread(new WXMessageLogHelper(doc, false, "true", "SUBSCRIBE"))
				.start();// 保存日志
		String email = checkEmail(message);
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (wxfakeid == null || wxfakeid.equals("")) {
			System.out.println("fakeid 没有查到");
			return null;
		}
		WXUserinfo user = null;
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		try {
			user = WXInfoDownloader.userWithOpenId.get(openId);
		} catch (Exception e) {
			try {
				user = (WXUserinfo) sqlMap.queryForObject(
						"getwxuserinfobyopenid", openId);
				WXInfoDownloader.userWithOpenId.put(openId, user);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		if (email != null) {
			Map<String, String> args = new HashMap<String, String>();
			args.put("email", email);
			args.put("openid", openId);
			user.setEmail(email);
			try {
				sqlMap.update("updatewxuseremail", args);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (message.toUpperCase().startsWith("M")) {
				try {
					email = user.getEmail();
					System.out.println(email);
				} catch (Exception e) {
				}
				if (email == null || "".equals(email)) {
					
					responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "command_error", null), openId, this.wxMsgId, "SUBSCRIBE", "EXPIRED");
//					responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "command_error", null));
					try {
						Document respDoc = DocumentHelper.parseText(responseXml);
						new Thread(new WXMessageLogHelper(respDoc, true, "true", "SUBSCRIBE","EXPIRED",this.wxMsgId)).start();// 保存日志
					} catch (DocumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return responseXml;
				}
			}
		}
		WXProcessHandler.getAircompanyList();
		Map<String, Object> command = WXProcessHandler.commandChains
				.get(commandForChains);
		// if(command == null &&
		// !message.toUpperCase().startsWith("Y") &&
		// commandChains.get("Y"+openId) != null &&
		// (new Date().getTime() - (Long)
		// commandChains.get("Y"+openId).get("datetime")) >= 0){
		// commandChains.remove("Y"+openId);
		// } else
		JSONObject awbData = new JSONObject();
		awbData.put("userid", wxfakeid);
		awbData.put("ops", "1");
		awbData.put("resource", "WEIXIN");
		if(email != null && !"".equals(email))
			awbData.put("email", email);
		if (command != null) {
			if ((new Date().getTime() - (Long) command.get("datetime")) < 0) {
				if (command.get("command").equals("subscribe")) {
					String code = command.get("key").toString();
					try {
						boolean result = WXProcessHandler.GetSubStatus(wxfakeid,code.substring(0, 3) + '-' + code.substring(3));
						if(result) 
							i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_already", new Object[]{code.substring(0, 3) + '-' + code.substring(3)});
//							responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "sub_already", new Object[]{code.substring(0, 3) + '-' + code.substring(3)}), openId, this.wxMsgId, "SUBSCRIBE", "SUCCESS");
//							responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "sub_already", new Object[]{code.substring(0, 3) + '-' + code.substring(3)}));
						else{
							awbData.put("awbnum", code.substring(0, 3) + '-' + code.substring(3));
							UserAWBHistoryServlet.saveUserHis(awbData);
							i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_success", new Object[]{code.substring(0, 3) + '-' + code.substring(3)});
//							responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "sub_success", new Object[]{code.substring(0, 3) + '-' + code.substring(3)}), openId, this.wxMsgId, "SUBSCRIBE", "SUCCESS");
//							responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "sub_success", new Object[]{code.substring(0, 3) + '-' + code.substring(3)}));
						}
						WXProcessHandler.commandChains.remove(commandForChains);
						} catch (DocumentException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_command_expired", null);
				if (email != null && !email.equals("")) {
					i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_command_expired_with_email", null);;
				}
				errorMsgType = "EXPIRED";
//				responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, expiredMessage, openId, this.wxMsgId, "SUBSCRIBE", "EXPIRED");
//				responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), expiredMessage);
			}
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, i18nMessage, openId, this.wxMsgId, "SUBSCRIBE", errorMsgType);
			
			WXProcessHandler.commandChains.remove(commandForChains);
			return responseXml;
		}
		message = message.toUpperCase().replaceAll(" ", "").replaceAll("-", "")
				.replaceAll("SUB", "").replaceAll("订阅", "").trim();
		Pattern p = Pattern.compile("\\d{11}");
		Matcher m = p.matcher(message);
		if (m.matches()) {
			try {
				if (message.length() == 11) {
					if(WXProcessHandler.getAircompanyList().get(message.substring(0, 3)) == null) {
						
//						responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "aircompany_notsupport", null), openId, this.wxMsgId, "SUBSCRIBE", "SUBFAIL");
						i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "aircompany_notsupport", null);
						errorMsgType = "SUBFAIL";
//						responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "aircompany_notsupport", null));
					}else {
						boolean result = WXProcessHandler.GetSubStatus(wxfakeid,message.substring(0, 3) + '-' + message.substring(3));
						
						if(result) 
							i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_already", new Object[]{message.substring(0, 3) + '-' + message.substring(3)});
//							responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "sub_already", new Object[]{message.substring(0, 3) + '-' + message.substring(3)}), openId, this.wxMsgId, "SUBSCRIBE", "SUCCESS");
//							responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "sub_already", new Object[]{message.substring(0, 3) + '-' + message.substring(3)}));
						else{
							awbData.put("awbnum", message.substring(0, 3) + '-' + message.substring(3));
							UserAWBHistoryServlet.saveUserHis(awbData);
//							WXProcessHandler.GetAwbTraceData(message.substring(0, 3) + '-' + message.substring(3), wxfakeid, true, openId,
//									this.wxMsgId, "SUBSCRIBE", email);
							i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_success", new Object[]{message.substring(0, 3) + '-' + message.substring(3)});
//							responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "sub_success", new Object[]{message.substring(0, 3) + '-' + message.substring(3)}), openId, this.wxMsgId, "SUBSCRIBE", "SUCCESS");
//							responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "sub_success", new Object[]{message.substring(0, 3) + '-' + message.substring(3)}));
						}
					}
					
				} else {
					i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_failed", null);
					errorMsgType = "SUBFAIL";
//					responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"),WeixinI18nUtil.getMessageWithOpenid(openId, "sub_failed", null));
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			i18nMessage = WeixinI18nUtil.getMessageWithOpenid(openId, "sub_failed", null);
			errorMsgType = "SUBFAIL";
//			responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "sub_failed", null));
		}
		responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, i18nMessage, openId, this.wxMsgId, "SUBSCRIBE", errorMsgType);
		return responseXml;
	}

	/**
	 * 异步处理。 首先判断消息体是否是y或者m（用于对前一次查询运单后的订阅）。y为微信消息订阅，m为邮件订阅。
	 * 判断消息中是否之前查过订单，如果查过，则通过SubscribeAwb方法订阅之前的消息。 如果不是y或m则判断是否是sub或者'订阅'指令 ＋
	 * 11位运单号，通过SubscribeAwb方法进行对某运单的订阅。如果不是则返回错误的帮助消息。
	 */
	public void asnycProcess() {
		// 首先判断是否是回复订阅消息
		message = message.trim();
		String commandForChains = "";

		if (message.toUpperCase().startsWith("Y")
				|| message.toUpperCase().startsWith("M"))
			commandForChains = "Y" + openId;
		new Thread(new WXMessageLogHelper(doc, false, "true", "SUBSCRIBE"))
				.start();// 保存日志
		String email = checkEmail(message);
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (wxfakeid == null || wxfakeid.equals("")) {
			System.out.println("fakeid 没有查到");
			return;
		}
		WXUserinfo user = null;
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		try {
			user = WXInfoDownloader.userWithOpenId.get(openId);
		} catch (Exception e) {
			try {
				user = (WXUserinfo) sqlMap.queryForObject(
						"getwxuserinfobyopenid", openId);
				WXInfoDownloader.userWithOpenId.put(openId, user);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		if (email != null) {
			Map<String, String> args = new HashMap<String, String>();
			args.put("email", email);
			args.put("openid", openId);
			user.setEmail(email);
			try {
				sqlMap.update("updatewxuseremail", args);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (message.toUpperCase().startsWith("M")) {
				try {
					email = user.getEmail();
					System.out.println(email);
				} catch (Exception e) {
				}
				if (email == null || "".equals(email)) {
					String responseXml = WXProcessHandler.GetTextMessageDoc(
							wxfakeid, "命令输入错误！", openId, this.wxMsgId,
							"SUBSCRIBE", "EXPIRED");
					WXAPIServiceProcess service = new WXAPIServiceProcess();
					boolean success = service.process(responseXml).contains(
							"发送成功");
					return;
				}
			}
		}
		WXProcessHandler.getAircompanyList();
		Map<String, Object> command = WXProcessHandler.commandChains
				.get(commandForChains);
		// if(command == null &&
		// !message.toUpperCase().startsWith("Y") &&
		// commandChains.get("Y"+openId) != null &&
		// (new Date().getTime() - (Long)
		// commandChains.get("Y"+openId).get("datetime")) >= 0){
		// commandChains.remove("Y"+openId);
		// } else
		if (command != null) {
			if ((new Date().getTime() - (Long) command.get("datetime")) < 0) {
				if (command.get("command").equals("subscribe")) {
					String code = command.get("key").toString();
					try {
						this.SubscribeAwb(openId, code.substring(0, 3) + '-'
								+ code.substring(3), wxfakeid, email);
					} catch (DocumentException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				String expiredMessage = "订阅命令已过期。";
				if (email != null && !email.equals("")) {
					expiredMessage = "订阅命令已过期，但您输入的邮件地址将被更新，下次可使用新邮件地址接收订阅。";
				}
				String responseXml = WXProcessHandler.GetTextMessageDoc(
						wxfakeid, expiredMessage, openId, this.wxMsgId,
						"SUBSCRIBE", "EXPIRED");
				WXAPIServiceProcess service = new WXAPIServiceProcess();
				boolean success = service.process(responseXml).contains("发送成功");
			}
			WXProcessHandler.commandChains.remove(commandForChains);
			return;
		} else if (command == null && message.toUpperCase().indexOf("SUB") < 0
				&& message.toUpperCase().indexOf("订阅") < 0) {
			String responsexml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>SendWXImageAndTextMessageWithID</ServiceAction><ServiceData><msgid>10001628</msgid><wxfakeid>"
					+ wxfakeid
					+ "</wxfakeid><openid>"
					+ openId
					+ "</openid><wxMsgId>"
					+ wxMsgId
					+ "</wxMsgId><MsgType>HELP</MsgType></ServiceData></eFreightService>";
			WXAPIServiceProcess service = new WXAPIServiceProcess();
			service.process(responsexml);
			return;
		}
		message = message.toUpperCase().replaceAll(" ", "").replaceAll("-", "")
				.replaceAll("SUB", "").replaceAll("订阅", "").trim();
		Pattern p = Pattern.compile("\\d{11}");
		Matcher m = p.matcher(message);
		if (m.matches()) {
			try {
				this.SubscribeAwb(openId, message.substring(0, 3) + '-'
						+ message.substring(3), wxfakeid, null);
			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,
					"订阅运单号有误", openId, this.wxMsgId, "SUBSCRIBE", "SUBFAIL");
			WXAPIServiceProcess service = new WXAPIServiceProcess();
			boolean success = service.process(responseXml).contains("发送成功");
		}
	}

	/**
	 * 判断字符串是否是email
	 * 
	 * @param message
	 * @return
	 */
	private String checkEmail(String message) {
		String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern regex = Pattern.compile(check);
		int position = 0;
		if (message.length() > 1 && message.toUpperCase().startsWith("M")
				&& (position = message.indexOf(" ")) > 0) {
			String str = message.substring(position + 1);
			Matcher matcher = regex.matcher(str);
			boolean isMatched = matcher.matches();
			if (isMatched)
				return str;
		}
		return null;
	}

	/**
	 * 订阅
	 * 
	 * @param openid
	 *            用户和平台在微信的唯一对应关系
	 * @param message
	 *            订单号
	 * @param wxfakeid
	 *            用户唯一id
	 * @param b
	 * @throws Exception
	 * @throws DocumentException
	 */
	private void SubscribeAwb(String openid, String message, String wxfakeid,
			String email) throws Exception, DocumentException {
		String responseXml = "";
		if (message.length() == 12) {
			WXProcessHandler.GetAwbTraceData(message, wxfakeid, true, openid,
					this.wxMsgId, "SUBSCRIBE", email);
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "运单"
					+ message + "已成功订阅，运单轨迹的变化会及时通知您！", openid, this.wxMsgId,
					"SUBSCRIBE", "SUCCESS");
		} else {
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,
					"订阅失败，您输入的运单号不是有效的运单！", openid, this.wxMsgId, "SUBSCRIBE",
					"SUBFAIL");
		}
		WXAPIServiceProcess service = new WXAPIServiceProcess();
		boolean success = service.process(responseXml).contains("发送成功");
		/**
		 * new Thread(new
		 * WXMessageLogHelper(DocumentHelper.parseText(responseXml), true,
		 * success)).start();
		 **/

	}

	// public static void main(String[] args) {
	// String message = "Y ludan@efreig ht.cn";
	// String check =
	// "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
	// Pattern regex = Pattern.compile(check);
	// int position = 0;
	// if (message.length() > 1 && message.toUpperCase().startsWith("Y") &&
	// (position = message.indexOf(" ")) > 0) {
	// String str = message.substring(position + 1);
	// Matcher matcher = regex.matcher(str);
	// boolean isMatched = matcher.matches();
	// if (isMatched)
	// System.out.println(str);
	// }
	// System.out.println("null");
	// }
}
