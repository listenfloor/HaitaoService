package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import oracle.sql.CLOB;

import org.dom4j.Document;
import org.dom4j.Node;

import com.efreight.SQL.InsertMessageLog;
import com.efreight.SQL.WXUploadFile;
import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 消息日志保存类
 * @author xianan
 *
 */
public class WXMessageLogHelper implements Runnable {

//	private static final String imageUrl = "/wximage/";
	/**
	 * 本地图片存储路径
	 */
	private static final String imageUrl = "/wximage";
	/**
	 * 服务器图片路径
	 */
	private static final String servicePath = "/WeixinService";
	/**
	 * xml的document
	 */
	private Document data;
	/**
	 * 是否是发送
	 */
	private boolean isSend;
	/**
	 * 是否发送成功
	 */
	private boolean success;
	/**
	 * 发送结果
	 */
	private String result;
	/**
	 * 消息类型.只有接收消息有消息类型，推送消息均没有消息类型。
	 */
	private String msgType;
	
	/**
	 * 返回错误类型。
	 */
	private String msgResult;
	
	/**
	 * 微信消息id
	 */
	private String wxMsgId;
	
	public WXMessageLogHelper(Document doc, boolean send, String result,String messageType) {
		this.data = doc;
		this.isSend = send;
		this.result = result;
		this.msgType = messageType;
	}
	
	public WXMessageLogHelper(Document doc, boolean send, String result,String messageType,String msgResult,String wxMsgId) {
		this.data = doc;
		this.isSend = send;
		this.result = result;
		this.msgType = messageType;
		this.msgResult = msgResult;
		this.wxMsgId = wxMsgId;
	}

	@Override
	/**
	 * 日志纪录分为两种，一种接收的，一种发送的。
	 * 根据<xml>节点来区分。如果有，说明是接收或同步返回的，如果没有说明是发送的。
	 * 接收消息中，根据不同的消息类型，会有不同的日志处理。例如图片，要增加本地图片查看连接。地表，增加地图查看连接。
	 * 发送消息，纪录发送结果。
	 */
	public void run() {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();

			String datetime = format.format(new Date());
			Node node = data.selectSingleNode("/xml");
			String sender = "";
			String receiver = "";
			String messagetype = "";
			String content = "";
			String wxMsgId=null;
			String msgResult = "NORMAL";
			try{
				msgResult = data.selectSingleNode("//ErrorMsgType").getText();
				System.out.println(msgResult);
			}catch(Exception e){
			}
			if (node != null) {//有xml的，表示为微信所发或同步返回log
//				<xml><ToUserName><![CDATA[gh_50738c3e80e3]]></ToUserName><FromUserName><![CDATA[omQjGji2ryvc9pFo2TRWT5RCoU9M]]></FromUserName><CreateTime>1378366997</CreateTime><MsgType><![CDATA[event]]></MsgType><Event><![CDATA[CLICK]]></Event><EventKey><![CDATA[MENU_TRACE_CLICK]]></EventKey></xml>
				String openid = isSend ? data.selectSingleNode("//ToUserName").getText() : data.selectSingleNode(
						"//FromUserName").getText();
				//isSend为真，是系统发给用户，假，是用户发给系统				//消息日志分为两种： 1 接收到的消息，判断是否是接收消息。 2 发送消息
				//系统接收消息log 需改造。
				String msgType = data.selectSingleNode("//MsgType").getText();
				String msgId = "";
				try{
					msgId = data.selectSingleNode("//MsgId").getText();
				}catch(Exception e){
				}
				if (isSend) {
					receiver = openid;
					sender = "gh_50738c3e80e3";
					//这里的是微信发来请求后所带消息ID
					try{
						if(this.wxMsgId!=null&&!"".equals(this.wxMsgId)){
							wxMsgId = this.wxMsgId;
						}
						if(this.msgResult!=null&&!"".equals(this.msgResult)){
							msgResult = this.msgResult;
						}
						//这里判断是否是同步返回的消息文本，分为多图文，单图文，文本。
					}catch(Exception e){
					}
				} else {
					sender = openid;
					receiver = "gh_50738c3e80e3";
					wxMsgId = msgId;
				}
				if(msgType!=null){
					if(msgType.equals("text")){//文本消息
						messagetype = "text";
						content = data.selectSingleNode("//Content").getText();
					}else if(msgType.equals("image")){//图片
						messagetype = "image";
						String picUrl = data.selectSingleNode("//PicUrl").getText();
						content = "<a href=\""+picUrl+"\" target=\"_blank\" >服务器图片位置</a>,<a target=\"_blank\"  href=\""+imageUrl+"/"+datetime.substring(0,4)+"/"+datetime.substring(5,7)+"/"+datetime.substring(8,10)+"/"+openid+"_"+msgId+".jpg"+"\">本地图片</a>";
					}else if(msgType.equals("voice")){//音频
						messagetype = "voice";
						content = "语音：" + data.selectSingleNode("//Recognition").getText();
					}else if(msgType.equals("location")){//位置
						messagetype = "location";
						String x = data.selectSingleNode("//Location_X").getText();
						String y = data.selectSingleNode("//Location_Y").getText();
						content = "<a target=\"_blank\"  href=\""+servicePath+"/UserLocationServlet?x="+x+"&y="+y+"\" >地理位置,x:"+x+" , y:"+y+"</a>";
					}else if(msgType.equals("news")){//图文消息
						messagetype = "imageandtext";
						List<Node> nodeList = data.selectNodes("//item");
						if(nodeList!=null&&nodeList.size()>0){
							for(Node nod: nodeList){
								String mesg = nod.selectSingleNode("//Title").getText();
								content+=" "+mesg;
							}
						}else{
							content = "空消息";
						}
					}else if(msgType.equals("event")) {
						messagetype = "menuclick";
						content = "自定义菜单点击：" + data.selectSingleNode("//EventKey").getText();
					}
				}
			} else {
				sender = "gh_50738c3e80e3";
				receiver = data.selectSingleNode("//openid") != null ? data.selectSingleNode("//openid").getText() : "";
				this.isSend = true;
				try{
					msgType = data.selectSingleNode("//MsgType").getText();
					wxMsgId = data.selectSingleNode("//wxMsgId").getText();
				}catch (Exception e) {
					
				}
				if(receiver.equals("")) {
					String fakeid = data.selectSingleNode("//wxfakeid").getText();
					 WXUserinfo userinfo = (WXUserinfo) sqlMap.queryForObject("getwxuserinfobyfakeid", fakeid);
					 receiver = userinfo.getOpenid();
				}
				String action = data.selectSingleNode("//ServiceAction").getText();
				if (action.equals("SendWXImageAndTextMessageWithID")) {
					messagetype = "imageandtext";
					content = "图文消息，消息ID=" + data.selectSingleNode("//msgid").getText();
				} else if (action.equals("SendWXImageAndTextMessage") || action.equals("SendWXImageAndTextMessageWithCustomAPI")) {
					messagetype = "imageandtext";
					content = "图文消息，消息内容=" + data.selectSingleNode("//title").getText() + "   "
							+ data.selectSingleNode("//digest").getText();
				} else if(action.equals("SendTracePushMessageWithTemplateAPI")) {
					messagetype = "text";
					content = "模版消息=您订阅的运单"+data.selectSingleNode("//awb_code").getText()+"有新的轨迹："+data.selectSingleNode("//stardard_data").getText()+"回复\"详细\"或直接回复运单号查看运单轨迹明细。";
				} else if(action.equals("SendWXMessage")){
					String message = "";
					String type = "";
					String fileid = "";
					String fileUrl = "";
					try{
						message = data.selectSingleNode("//message").getText();
						type=data.selectSingleNode("//type").getText();
						fileid = data.selectSingleNode("//fileid").getText();
					}catch (Exception e) {
						
					}
					messagetype = "REPLY";
					//这里增加一个查询文件路径
					if(fileid!=null&&!"".equals(fileid)){
						Map<String,String> map = new HashMap<String, String>();
						map.put("wxFileId", fileid);
						WXUploadFile file = null ;
						try{
							file = (WXUploadFile)sqlMap.queryForObject("loadwxuploadfile",map);
							if(file!=null&&file.getFileUrl()!=null&&!"".equals(file.getFileUrl())){
								fileUrl = file.getFileUrl();
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					if(type!=null){
						if("1".equals(type)){
							content = "人工回复文本消息，消息="+message;
						}else if("2".equals(type)){
							content = "人工回复图片消息，消息id="+fileid+" <a href=\""+fileUrl+"\" target=\"_blank\">图片</a>";
						}else if ("3".equals(type)){
							content = "人工回复声音消息，消息id="+fileid+" <a href=\""+fileUrl+"\" target=\"_blank\">声音</a>";
						}else if("4".equals(type)){
							content = "人工回复视频消息，消息id="+fileid+" <a href=\""+fileUrl+"\" target=\"_blank\">视频</a>";
						}
						
					}
				} else {
					messagetype = "text";
					content = data.selectSingleNode("//message").getText();
				}
			}
			// Map<String, String> args = new HashMap<String, String>();
//			String sql = "insert into messagelog (messageid,datetime,sender,receiver,messagetype,content,success) values (seq_wxmessage.nextval,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?,?)";
			// 获得数据源
			boolean success = result!=null&&(result.equals("true") || result.contains("\"msg\":\"ok\"")||result.contains("preview send success")) || result.contains("\"errmsg\":\"ok\"");
			System.out.println("msgType : "+msgType);
			Map<String,String> entity = new HashMap<String, String>();
			entity.put("datetime", datetime);
			entity.put("sender", sender);
			entity.put("receiver", receiver);
			entity.put("messagetype", messagetype);
			entity.put("content", content);
			entity.put("success", success?"true":"false");
			entity.put("xml", data.asXML());
			entity.put("wxMsgId", wxMsgId);
			entity.put("msgType", msgType);
			entity.put("issend", this.isSend==true?"1":"0");
			entity.put("msgresult", msgResult);
			entity.put("sendresult",result);
		    //新增记录
			InsertMessageLog iml = new InsertMessageLog();
			try{
				iml.insertXML(entity);
			}catch(Exception e){
				success = false;
			}
//		    sqlMap.insert("insertmessagelog",entity);
//			PreparedStatement pstmt = conn.prepareStatement(sql);
//			pstmt.setString(1, datetime);
//			pstmt.setString(2, sender);
//			pstmt.setString(3, receiver);
//			pstmt.setString(4, messagetype);
//			pstmt.setString(5, content);
//			//pstmt.setCLOB(6, this.getCLOB(data.asXML(), conn));
//			pstmt.setString(6, String.valueOf(success));
//			pstmt.executeUpdate();
			Map<String, String> args = new HashMap<String, String>();
			args.put("datetime", datetime);
			args.put("sender", sender);
			args.put("receiver", receiver);
			args.put("messagetype", messagetype);
			args.put("content", content);
			args.put("success", result);
			args.put("xml", data.asXML());
			args.put("wxMsgId", wxMsgId);
			// sqlMap.insert("WXAPIService.insertmessagelog", args);
			
//			if(!this.isSend &&(messagetype.equals("location")||messagetype.equals("image")||messagetype.equals("voice"))) {
//				MailComponent component  = new MailComponent("smtp.ym.163.com", "report@efreight.cn", "pass@word1");
//				String mailContent = WXInfoDownloader.userWithOpenId.get(sender).getNickname() + "于" + datetime + "发送了一条" + messagetype + "消息，请注意回复！";
//				component.smtp(PropertiesUtils.readProductValue("", "messagereportreciever"), "有新的" + messagetype +"消息", mailContent, false);
//			}
//			if (!success) {
//				String warningXml = "<Service>" + "<ServiceURL>MailService</ServiceURL>"
//						+ "<ServiceAction>send</ServiceAction>" + "<ServiceData>" + "<MailService>"
//						+ "<mailserver>smtp.ym.163.com</mailserver>" + "<sender>service4@efreight.me</sender>"
//						+ "<senderpassword>123456</senderpassword>"
//						+ "<receiverlist>ludan@efreight.cn;renyj@efreight.cn;yes@efreight.cn</receiverlist>"
//						+ "<subject>微信平台信息发送失败预警</subject>" + "<content>信息内容:" + args.toString() + "</content>"
//						+ " </MailService>" + "</ServiceData>" + "</Service>";
//				HttpHandler.postHttpRequest("http://app.efreight.me/HttpEngine", warningXml);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}

	}


	public static CLOB getCLOB(String innStr, Connection conn) throws Exception {
		CLOB tempClob = null;
		// If the temporary CLOB has not yet been created, create new
		tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);

		// Open the temporary CLOB in readwrite mode to enable writing
		tempClob.open(CLOB.MODE_READWRITE);
		// Get the output stream to write
		Writer tempClobWriter = tempClob.getCharacterOutputStream();
		// Write the data into the temporary CLOB
		tempClobWriter.write(innStr);

		// Flush and close the stream
		tempClobWriter.flush();
		tempClobWriter.close();

		// Close the temporary CLOB
		tempClob.close();
		return tempClob;
	}
}
