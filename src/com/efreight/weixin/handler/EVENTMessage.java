package com.efreight.weixin.handler;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.CerInfoEntity;
import com.efreight.weixin.CerInfoManager;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.WXSubscribeLogHelper;
import com.efreight.weixin.WXUserinfo;
import com.efreight.weixin.menuclickhandler.IMenuClickHandler;
import com.efreight.weixin.process.WXProcessHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 处理订阅和取消订阅类，继承WXMessageHandler的父类
 * @author keller
 *
 */
public class EVENTMessage extends WXMessageHandler{

	private Logger log = Logger.getLogger(EVENTMessage.class);
	/**
	 * 唯一构造方法
	 * @param doc 微信请求过来的xml转换成dom4j的Document类型。
	 * @param url 
	 */
	public EVENTMessage(Document doc,String url){
		this.doc = doc;
		this.url=url;
	}
	
	/**
	 * 实现父类方法。流程为：
	 * 如果是关注请求，调用GetWelcomeTextMessage方法，同步返回欢迎语句。再调用WXAPIServiceProcess处理获取微信用户fakeid和openid对应关系。
	 * 如果是取消关注，直接纪录用户取消/关注日至（操作数据库subscribelog表）
	 */
	public String process(){
//		long timeStart = System.currentTimeMillis();//设置开始时间
		System.out.println("-------------------event message-------------------");
		Node eventNode = doc.selectSingleNode("//Event");
		String event = eventNode == null ? null : eventNode.getText();
		String responsexml = "";
		if (event != null) {
			String openid = doc.selectSingleNode("//FromUserName").getText();
			if (event.equalsIgnoreCase("subscribe") || event.equalsIgnoreCase("scan")) {
				Node ticketNode = doc.selectSingleNode("//Ticket");
				try {
					if(WXInfoDownloader.userWithOpenId.get(openid) == null){
						responsexml = this.GetWelcomeTextMessage(openid);
						SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
						
						if(event.equalsIgnoreCase("scan")) {
							//insert into scaninfo
							String ticket = ticketNode.getText();
							String nopenid = (String)sqlMap.queryForObject("getmarketingqrcodeopenid",ticket);
							if(nopenid != null && !"".equals(nopenid)) {
								Map args = new HashMap();
								args.put("openid", openid);
								args.put("qrcodekey", ticket);
								args.put("datetime", new Date());
								sqlMap.insert("insertscaninfo",args);
							}
						}else{ 
							WXInfoDownloader userinfodownloader = new WXInfoDownloader();
							WXUserinfo wxuser = userinfodownloader.GetWXUserinfoByOpenId(openid);
							Thread newThread = new Thread(wxuser);
							newThread.run();
						}
					}
					if(ticketNode != null) {
						String EventKey = this.doc.selectSingleNode("//EventKey").getStringValue();
			             System.out.println("EventKey = " + EventKey);
			 
			             String requestXml = "<eFreightService><ServiceAction>TRANSACTION</ServiceAction><ServiceData><LinkedAccount><linkedaccountid>" + 
			               openid + 
			               "</linkedaccountid>" + 
			               "<linkedaccounttype>WECHAT</linkedaccounttype>" + 
			               "<operatetime>sysdate</operatetime>" + 
			               "<usersyscode>" + 
			               EventKey + 
			               "</usersyscode>" + 
			               "</LinkedAccount>" + 
			               "</ServiceData>" + 
			               "<ServiceURL>LinkedAccount</ServiceURL>" + 
			               "</eFreightService>";
			             System.out.println("responsexml = " + requestXml);
			             String responseData = HttpHandler.postHttpRequest("http://www.eft.cn/eFreightUserEngine", requestXml);
			             System.out.println("responseData = " + responseData);
			 
			             Document doc = DocumentHelper.parseText(responseData);
			             String username = doc.selectSingleNode("//username").getText();
			 
			             responsexml = GetEfreightChinaBindingTextMessage(openid, username);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new Thread(new WXSubscribeLogHelper(doc)).start();
				final String id = openid;
				new Thread(new Runnable() {
					
					public void run() {
//						String bindopenidxml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>GetWXUserinfoWithOpenid</ServiceAction><ServiceData><openid>"
//								+ id + "</openid><MsgType>EVENT</MsgType><content></content></ServiceData></eFreightService>";
//						WXAPIServiceProcess service = new WXAPIServiceProcess();
//						String returnMessage = service.process(bindopenidxml);
						try {
							WXInfoDownloader downloader = new WXInfoDownloader();
							downloader.GetWXUserinfoByOpenId(id);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
			else if(event.equalsIgnoreCase("unsubscribe")){
				Map<String, String> args = new HashMap<String, String>();
				SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
				WXUserinfo userinfo = null;
				try {
					userinfo = (WXUserinfo)sqlMap.queryForObject("getwxuserinfobyopenid",openid);
					if(userinfo!=null&&userinfo.getFakeid()!=null&&!"".equals(userinfo.getFakeid())){
						log.info(userinfo);
						String requestXml = "<eFreightService><ServiceURL>Subscribe</ServiceURL><ServiceAction>CancleFollow</ServiceAction><ServiceData><Subscribe><subscriber>"+userinfo.getFakeid()+"</subscriber><subscribertype>WEIXIN</subscribertype></Subscribe></ServiceData></eFreightService>";
						log.info(requestXml);
						String responseData = HttpHandler.postHttpRequest(PropertiesUtils.readProductValue("", "awbtraceurl"), requestXml);
						log.info(responseData);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String subscribetype = doc.selectSingleNode("//Event").getText();
				args.put("openid", openid);
				args.put("datetime", format.format(new Date()));
				args.put("optype", subscribetype);
//				WXInfoDownloader.userWithFakeId.remove(WXInfoDownloader.userWithOpenId.get(openid).fakeid);
				WXInfoDownloader.userWithOpenId.remove(openid);
				try {
					sqlMap.update("updateuserstatus",openid);
					sqlMap.update("insertsubscribelog", args);
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}else if(event.equalsIgnoreCase("CLICK")) {
				String menuKey = doc.selectSingleNode("//EventKey").getText();
		        System.out.println("menuKey =" + menuKey);
				if (menuKey.equals("MENU_UPADTECER_CLICK"))
				{
				   CerInfoEntity cerInfoEntity = new CerInfoEntity();
				   cerInfoEntity.setIsreadytoupdate(Boolean.valueOf(true));
				   cerInfoEntity.setCertype(1);
				   CerInfoManager.getInstance().setConfig(openid, cerInfoEntity);
				}
				else
				{
				   CerInfoEntity cerInfoEntity = new CerInfoEntity();
				   cerInfoEntity.setIsreadytoupdate(Boolean.valueOf(false));
				   cerInfoEntity.setCertype(1);
				   CerInfoManager.getInstance().setConfig(openid, cerInfoEntity);
				}
				String message = WeixinI18nUtil.getMessageWithOpenid(openid, menuKey, null);
				try
				{
				   message = message.replace("[openid]", openid);
				}
				catch (Exception localException1)
				{
				}
				int position = message.indexOf(":");
				String type = message.substring(0,position);
				message = message.substring(position+1);
				if(type.equalsIgnoreCase("image")) {
					JSONArray objs = new JSONArray(message);
					List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
					for (int i = 0; i < objs.length(); i++) {
						JSONObject obj = objs.getJSONObject(i);
						
								Map<String,String> tactMap = new HashMap<String,String>();
								tactMap.put("title", obj.getString("title"));
								tactMap.put("content", obj.getString("description"));
								tactMap.put("picUrl", obj.getString("picurl"));
								tactMap.put("url", obj.getString("url"));
								messageList.add(tactMap);
							}
					
					responsexml = WXProcessHandler.getWXTextAndImageXML(openid, messageList);
				}else if(type.equalsIgnoreCase("text")){
					
					responsexml = WXProcessHandler.getWXTextResponseXML(openid, PropertiesUtils.readProductValue("", "fromuser"), message);
				}else if(type.equalsIgnoreCase("event")) {
					Class<IMenuClickHandler> c;
					try {
						c = (Class<IMenuClickHandler>)Class.forName(message);
						IMenuClickHandler handler = c.newInstance();
						String[] response = handler.process(openid, menuKey);
						WXAPIServiceProcess process = new WXAPIServiceProcess();
						for (String msg : response) {
							process.process(msg);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				
				new Thread(new WXMessageLogHelper(doc, false, "true", "MENUCLICK", menuKey, "0")).start();
			}
		}
		return responsexml;
	}
	
	private String GetWelcomeTextMessage(String openid) throws Exception {
		String responsexml = "<xml>" + "<ToUserName><![CDATA[" + openid + "]]></ToUserName>"
				+ "<FromUserName><![CDATA["+PropertiesUtils.readProductValue("", "fromuser")+"]]></FromUserName>" + "<CreateTime>" + new Date().getTime()
				/ 1000 + 3 + "</CreateTime>" + "<MsgType><![CDATA[text]]></MsgType>"
				+ "<Content><![CDATA[" + WeixinI18nUtil.getMessageWithOpenid(openid, "welcomemessage", new Object[]{openid})
				+ "]]></Content>" + "<FuncFlag>0</FuncFlag>" + "</xml>";
		//发送日期，消息类型event 为关注
		new Thread(new WXMessageLogHelper(DocumentHelper.parseText(responsexml), true,"true","EVENT")).start();
		return responsexml;
	}
	
	private String GetEfreightChinaBindingTextMessage(String openid, String username) throws Exception {
	     String responsexml = "<xml><ToUserName><![CDATA[" + openid + "]]></ToUserName>" + 
	       "<FromUserName><![CDATA[" + PropertiesUtils.readProductValue("", "fromuser") + "]]></FromUserName>" + "<CreateTime>" + new Date().getTime() / 
	       1000L + 3 + "</CreateTime>" + "<MsgType><![CDATA[text]]></MsgType>" + 
	       "<Content><![CDATA[" + "[username]与指尖海淘微信公众号绑定成功".replace("[username]", username) + 
	       "]]></Content>" + "<FuncFlag>0</FuncFlag>" + "</xml>";
	 
	     new Thread(new WXMessageLogHelper(DocumentHelper.parseText(responsexml), true, "true", "EVENT")).start();
	     return responsexml;
	   }
}
