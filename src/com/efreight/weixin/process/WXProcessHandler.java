package com.efreight.weixin.process;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.UserAWBHistoryServlet;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 处理请求帮助类
 * 
 * @author xianan
 * 
 */
public class WXProcessHandler {
	/**
	 * 纪录用户之前查询过运单信息，为回复m/y指令使用
	 */
	public static Map<String, Map<String, Object>> commandChains = new HashMap<String, Map<String, Object>>();
	
	/**
	 * 纪录用户查询运单时间。如果超时，则不发送并提示人工处理。
	 */
	public static Map<String,Date> requestTimeChains = new HashMap<String,Date>();
	/**
	 * 支持航空公司列表
	 */
	public static Map<String, String> aircompanyList = null;
	/**
	 * 
	 */
//	public static String airCompanyTip = "系统暂不支持该航空公司的运单轨迹查询与订阅。";
	
	private static Date aircompanyUpdateTime = null;
	
	/**
	 * 获取微信用户的fakeid
	 * 
	 * @param openid
	 *            openid
	 * @param content
	 *            文本内容
	 * @return String
	 * @throws Exception
	 */
	public static String GetWXFakeidWithOpenid(String openid, String content)
			throws Exception {
		Document doc = GetWXUserInfo(openid, content);
		Node node = doc.selectSingleNode("//fakeid");
		if (node != null)
			return node.getText();
		return null;
	}

	/**
	 * 获取微信用户信息
	 * 
	 * @param openid
	 * @param content
	 * @return
	 * @throws Exception
	 * @throws DocumentException
	 */
	private static Document GetWXUserInfo(String openid, String content)
			throws Exception, DocumentException {
		String xml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>GetWXUserinfoWithOpenid</ServiceAction><ServiceData><WXUserInfo><openid>"
				+ openid
				+ "</openid><content>"
				+ content
				+ "</content></WXUserInfo></ServiceData></eFreightService>";
		WXAPIServiceProcess service = new WXAPIServiceProcess();
		String response = service.process(xml);
		Document doc = DocumentHelper.parseText(response);
		return doc;
	}

	/**
	 * 获取微信文本返回
	 * @param openId
	 * @param fromUser
	 * @param description
	 * @return
	 */
	public static String getWXTextResponseXML(String openId, String fromUser,  String description) {// 这里是单条消息
		StringBuffer sb = new StringBuffer("<xml>" + "<ToUserName><![CDATA[" + openId + "]]></ToUserName>"
				+ "<FromUserName><![CDATA["+fromUser+"]]></FromUserName>" + "<CreateTime>" + new Date().getTime()
				/ 1000 + 3 + "</CreateTime>" + "<MsgType><![CDATA[text]]></MsgType>"
				+ "<Content><![CDATA["+description
				+ "]]></Content>" + "<FuncFlag>0</FuncFlag>" + "</xml>");
		return sb.toString();
	}


	/**
	 * 多条回复消息
	 * 
	 * @param openId
	 * @param fromUser
	 * @param messageList
	 * @param errorMsgType 
	 * @param msgType 
	 * @param wxMsgId 
	 * @return String
	 */
	public static String getWXTextAndImageCustomApiXML(String openId,
			 List<Map<String, String>> messageList, String wxMsgId, String msgType, String errorMsgType) {// 这里是单条消息
		StringBuffer sb = new StringBuffer("<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>SendWXImageAndTextMessageWithCustomAPI</ServiceAction><ServiceData>");
				sb.append("<wxfakeid>"+WXInfoDownloader.userWithOpenId.get(openId).getFakeid()+"</wxfakeid>");
		if (messageList != null && messageList.size() > 0) {
			for (Map<String, String> msg : messageList) {
				sb.append("<messageitem>")
				.append("<title><![CDATA["+(msg.get("title") != null ? msg.get("title") : "")+"]]></title>")
				.append("<digest><![CDATA["+(msg.get("content") != null ? msg.get("content") : "")+"]]></digest>")
				.append("<url><![CDATA["+(msg.get("url") != null ? msg.get("url") : "")+"]]></url>")
				.append("<imgurl><![CDATA["+(msg.get("picUrl") != null ? msg.get("picUrl") : "")+"]]></imgurl>")
				.append("</messageitem>");
			}
		}

		sb.append("<wxMsgId>"+wxMsgId+"</wxMsgId>")
			.append("<MsgType>"+msgType+"</MsgType>")
			.append("<ErrorMsgType>"+errorMsgType+"</ErrorMsgType>")
			.append("<openid>"+openId+"</openid>")
			.append("</ServiceData>")
			.append("</eFreightService>");
		
		return sb.toString();
		
	}
	
	public static String getWXTextAndImageXML(String openId,List<Map<String,String>> messageList) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < messageList.size(); i++) {
			Map<String,String> item = messageList.get(i);
			sb.append("<item>");
			sb.append("<Title><![CDATA["+(item.get("title") != null ? item.get("title") : "")+"]]></Title>");
			sb.append("<Description><![CDATA["+(item.get("content") != null ? item.get("content") : "")+"]]></Description>");
			sb.append("<PicUrl><![CDATA["+(item.get("picUrl") != null ? item.get("picUrl") : "")+"]]></PicUrl>");
			sb.append("<Url><![CDATA["+(item.get("url") != null ? item.get("url") : "")+"]]></Url>");
			sb.append("</item>");
			
		}
		String response = "<xml>"
				+ "<ToUserName><![CDATA["+openId+"]]></ToUserName>"
				+ "<FromUserName><![CDATA["+ PropertiesUtils.readProductValue("", "fromuser")+"]]></FromUserName>"
				+ "<CreateTime>" + new Date().getTime()
				/ 1000 + 3 + "</CreateTime>"
				+ "<MsgType><![CDATA[news]]></MsgType>"
				+ "<ArticleCount>"+messageList.size()+"</ArticleCount><Articles>" + sb.toString()
				+"</Articles>"
				+ "</xml> ";
		
		return response;
	}
	
	public static String GetWXImageMessageCustomApiXML(String wxfakeid, String message,
			String openid, String wxMsgId, String msgType, String errorMsgType) {
		return "<eFreightService><ServiceURL>WXAPIService</ServiceURL>	<ServiceAction>SendWXImageMessageWithCustomAPI</ServiceAction><ServiceData><WXUserInfo><message><![CDATA["
				+ message
				+ "]]></message><wxfakeid>"
				+ wxfakeid
				+ "</wxfakeid><openid>"
				+ openid
				+ "</openid><wxMsgId>"
				+ wxMsgId
				+ "</wxMsgId><MsgType>"
				+ msgType
				+ "</MsgType><ErrorMsgType>"
				+ errorMsgType
				+ "</ErrorMsgType></WXUserInfo></ServiceData></eFreightService>";
	}
	/**
	 * 获取推送消息xml,增加errorMsgType
	 * 
	 * @param wxfakeid
	 * @param message
	 * @param openid
	 * @return String
	 */
	public static String GetTextMessageDoc(String wxfakeid, String message,
			String openid, String wxMsgId, String msgType, String errorMsgType) {
		return "<eFreightService><ServiceURL>WXAPIService</ServiceURL>	<ServiceAction>SendWXTextMessage</ServiceAction><ServiceData><WXUserInfo><message><![CDATA["
				+ message
				+ "]]></message><wxfakeid>"
				+ wxfakeid
				+ "</wxfakeid><openid>"
				+ openid
				+ "</openid><wxMsgId>"
				+ wxMsgId
				+ "</wxMsgId><MsgType>"
				+ msgType
				+ "</MsgType><ErrorMsgType>"
				+ errorMsgType
				+ "</ErrorMsgType></WXUserInfo></ServiceData></eFreightService>";
	}

	/**
	 * 获取航空公司map
	 * 
	 * @return Map<String, String>
	 */
	public static Map<String, String> GetAirCompanyMap() {
		Map<String, String> result = new HashMap<String, String>();
		String xml = "<Service><ServiceURL>TraceTranslate</ServiceURL><ServiceAction>querySupportAirCompany</ServiceAction><ServiceData></ServiceData></Service>";
		try {
			String resultXml = HttpHandler.postHttpRequest(PropertiesUtils.readProductValue("", "awbtraceurl"),xml);
			Document resultDoc = DocumentHelper.parseText(resultXml);
			List<Node> airCompanys = (List<Node>) resultDoc
					.selectNodes("//aircompany");
			for (Node airCompany : airCompanys) {
				String code = airCompany.selectSingleNode("ac_code3c")
						.getText();
				String name = airCompany.selectSingleNode("cnname").getText();
				result.put(code, name);
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, 5);
			aircompanyUpdateTime = calendar.getTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 订阅/取消订阅 航空运单请求
	 * 
	 * @param message
	 *            消息体
	 * @param wxid
	 *            微信ID
	 * @param subscribe
	 *            订阅/取消订阅
	 * @param openid
	 *            openId
	 * @param email
	 * @return String
	 * @throws Exception
	 */
	public static String GetAwbTraceData(String message, String wxid,
			boolean subscribe, String openid, String wxMsgId, String msgType,
			String email) throws Exception {
		WXProcessHandler.requestTimeChains.put(wxMsgId, new Date());
		String subscribertype = "WEIXIN";// subscribe ? "WEIXIN" : "NONE";
		String limit_num = subscribe ? "-1" : "1";
		String sync = subscribe ? "N" : "Y";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String requestXml = "<eFreightService>"
				+ "<ServiceURL>Subscribe</ServiceURL>"
				+ "<ServiceAction>TRANSACTION</ServiceAction>"
				+ "<ServiceData>" + "<Subscribe>"
				+ "<type>trace</type><target>" + message
				+ "</target><targettype>MAWB</targettype>" + "<sync>" + sync
				+ "</sync><subscriber>" + wxid + "</subscriber><wxMsgId>"
				+ wxMsgId + "</wxMsgId><subscribertype>" + subscribertype
				+ "</subscribertype>"
				+ "<standard_type>3</standard_type><limit_num>" + limit_num
				+ "</limit_num><offflag></offflag><systime>"+format.format(new Date())+"</systime></Subscribe>" + "</ServiceData>"
				+ "</eFreightService>";
		String responseData = HttpHandler
				.postHttpRequest(
						PropertiesUtils.readProductValue("", "awbtraceurl"),
						requestXml);
		
		if (subscribe && email != null) {
			requestXml = "<eFreightService>"
					+ "<ServiceURL>Subscribe</ServiceURL>"
					+ "<ServiceAction>TRANSACTION</ServiceAction>"
					+ "<ServiceData>" + "<Subscribe>"
					+ "<type>trace</type><target>" + message
					+ "</target><targettype>MAWB</targettype>" + "<sync>"
					+ sync + "</sync><subscriber>" + email
					+ "</subscriber><wxMsgId>" + wxMsgId
					+ "</wxMsgId><subscribertype>MAIL</subscribertype>"
					+ "<standard_type>3</standard_type><limit_num>" + limit_num
					+ "</limit_num><offflag></offflag><systime>"+format.format(new Date())+"</systime></Subscribe>" + "</ServiceData>"
					+ "</eFreightService>";
			HttpHandler.postHttpRequest(
					PropertiesUtils.readProductValue("", "awbtraceurl"),
					requestXml);
		}
		if (!subscribe){
			//获取weixin接收到发送的毫秒数
			String responseXml = GetAwbTraceHtmlData(responseData, wxid, openid, wxMsgId,msgType,false);
			long timer = getAWBMsgTime(wxMsgId);
			if(timer>=180000){//超过3分钟则不发请求并纪录log
				// 起线程发log
				new Thread(new WXMessageLogHelper(
						org.dom4j.DocumentHelper.parseText(responseXml), true, "false", null)).start();
				// 删除内存中消息时间
				removeAWBMsgTime(wxMsgId);
				return "";
			}
			return responseXml;
		}else{
			return "";
		}
	}
	

	public static String USubAwbTraceData(String message, String wxid,
			boolean subscribe, String openid, String wxMsgId, String msgType) throws Exception {
		String requestXml = "<eFreightService>"
				+ "<ServiceURL>Subscribe</ServiceURL>"
				+ "<ServiceAction>TRANSACTION</ServiceAction>"
				+ "<ServiceData><Subscribe>"
				+ "<type>trace</type><target>" + message
				+ "</target><targettype>MAWB</targettype>" + "<sync>N</sync><subscriber>" + wxid + "</subscriber><wxMsgId>"
				+ wxMsgId + "</wxMsgId><subscribertype>WEIXIN</subscribertype>"
				+ "<standard_type>2</standard_type><offflag>1</offflag><limit_num>-1</limit_num></Subscribe>" + "</ServiceData>"
				+ "</eFreightService>";
		String responseData = HttpHandler
				.postHttpRequest(
						PropertiesUtils.readProductValue("", "awbtraceurl"),
						requestXml);
		return responseData;
	}

	/**
	 * 发送航空运单订阅请求
	 * 
	 * @param message
	 *            消息体
	 * @param wxid
	 *            微信ID
	 * @param openid
	 *            openid
	 * @return String
	 * @throws Exception
	 */
	public static String GetAwbTraceData(String message, String wxid,
			String openid, String wxMsgId, String msgType) throws Exception {
		String result = null;
		try {
			
			String airCompanyName = aircompanyList.get(message.substring(0, 3));
			if (airCompanyName == null) {
				result = WXProcessHandler.GetTextMessageDoc(wxid,
						WeixinI18nUtil.getMessageWithOpenid(openid, "aircompany_notsupport", null), openid, wxMsgId, msgType, "UNSUPPORT");// 不支持的航空公司
			} else{
				result = WXProcessHandler.GetAwbTraceData(message, wxid, false,
						openid, wxMsgId, msgType, null);
			}
		} catch (Exception e) {
			result = "暂无轨迹信息";
			// result = WXProcessHandler.GetTextMessageDoc(wxid,
			// "暂无轨迹信息，请稍后再查询或回复Y订阅此运单，谢谢！", openid,wxMsgId,msgType,"NOTFOUND");
		}
		return result;
	}

	/**
	 * 根据查询运单消息的返回，组织发送给用户html体里面的xml
	 * 
	 * @param responseData
	 * @param wxid
	 * @param openid
	 * @param wxMsgId
	 * @param msgType
	 * @return
	 * @throws Exception
	 */
	public static String GetAwbTraceHtmlData(String responseData, String wxid,
			String openid, String wxMsgId, String msgType, boolean isSub)
			throws Exception {
		String htmlData = "";
		String responseXml = "";
		try {
			Document doc = DocumentHelper.parseText(responseData);
			String awbcode = doc.selectSingleNode("//AWB_CODE").getText();
			List<Node> tracesNodeList = doc.selectNodes("//TraceTranslate");
			if (tracesNodeList == null || tracesNodeList.size() == 0) {
				throw new Exception("");
			}
			String lastestDate = "";
			String lastestStatus = "";
			String listhtml = "";
			String last_stardard_data = "";
			SimpleDateFormat longformat = new SimpleDateFormat("MM-dd HH:mm");
			SimpleDateFormat shortformat = new SimpleDateFormat("MM-dd");
			for (int i = (tracesNodeList.size() - 1); i >= 0; i--) {
				Node traceNode = tracesNodeList.get(i);
				String occur_time = "";
				if (traceNode.selectSingleNode("OCCUR_TIME") != null
						&& traceNode.selectSingleNode("OCCUR_TIME").getText() != null
						&& !"".equals(traceNode.selectSingleNode("OCCUR_TIME"))) {
					occur_time = traceNode.selectSingleNode("OCCUR_TIME")
							.getText();
				}
				String cargo_code = traceNode.selectSingleNode("CARGO_CODE")
						.getText();
				String stardard_data = traceNode.selectSingleNode("TRACE_DATA")
						.getText();

				if (i == tracesNodeList.size() - 1) {
					last_stardard_data = traceNode
							.selectSingleNode("TRACE_DATA").getText()
							.replace("&lt;BR/&gt;", "\n");
					lastestDate = occur_time;

					if (stardard_data != null && stardard_data.length() > 12) {
						lastestStatus = stardard_data.substring(0, 12) + "...";
					} else {
						lastestStatus = stardard_data;
					}
				}

				listhtml += "<p><span style=\"color:#7f7f7f;font-size:12px;\">"
						+ stardard_data
						+ "</span></p><p><hr style=\"color:#d8d8d8; width:98%;border-style: dashed;\"/></p>";
			}
			String url = WeixinI18nUtil.getMessageWithOpenid(openid,
					"waybillurl", null);
			StringBuffer sb = new StringBuffer();
			sb.append("<messageitem>");
			if (isSub) {
				sb.append("<title>您订阅的 : " + awbcode + "有轨迹更新</title>");
				sb.append("<digest>最新轨迹 : " + lastestStatus + " </digest>");
			} else {
				sb.append("<title>"
						+ WeixinI18nUtil.getMessageWithOpenid(openid,
								"trace_title", new Object[] { awbcode })
						+ "</title>");
				sb.append("<digest>"
						+ WeixinI18nUtil.getMessageWithOpenid(openid,
								"trace_sub_tip", null) + "</digest>");
			}
			sb.append("<fileid>"
					+ PropertiesUtils.readProductValue("", "awbtracefileid")
					+ "</fileid>");
			sb.append("<content>"
					+ (new sun.misc.BASE64Encoder()).encode(listhtml
							.getBytes("UTF-8")) + "</content>");
			sb.append("<url>"
					+ url
					+ "?mawbcode="
					+ awbcode
					+ "*"
					+ wxid
					+ "*"
					+ wxMsgId
					+ "</url><imgurl>"
					+ "http://mmsns.qpic.cn/mmsns/LUkW1zMiauPqeaxFpQickX3yCNiaX74Bp1Yg4SzYlcPYgib5AdsAG48q5w/0"
					+ "</imgurl>");
			sb.append("</messageitem>");
			
			responseXml = "<eFreightService>"
					+ "<ServiceURL>WXAPIService</ServiceURL>"
					+ "<ServiceAction>SendWXImageAndTextMessage</ServiceAction>"
					+ "<ServiceData>" + "<wxfakeid>"
					+ wxid
					+ "</wxfakeid><messageid>10000125</messageid>"
					+ sb.toString()
					+ "<wxMsgId>"
					+ wxMsgId
					+ "</wxMsgId>"
					+ "<MsgType>"
					+ msgType
					+ "</MsgType><ErrorMsgType>NORMAL</ErrorMsgType><openid>"
					+ openid
					+ "</openid>"
					
					+ "<stardard_data>"
					+ last_stardard_data
					+ "</stardard_data><awb_code>"
					+ awbcode
					+ "</awb_code></ServiceData>"
					+ "</eFreightService>";

			// responseXml = WXProcessHandler.GetTextMessageDoc(
			// wxid,
			// "<![CDATA[点击查看运单<a href=\""
			// + PropertiesUtils
			// .readProductValue("", "waybillurl")
			// + "?mawbcode=" + code + "p" + wxid + "p" + wxMsgId
			// + "\">" + code + "</a>最新轨迹]]>", openid,
			// wxMsgId, "AWBTRACE", "NORMAL");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(responseData);
			throw new Exception("生成轨迹详细信息是出错：WXAPIServlet-->GetTraceHtmlData()"
					+ e.getMessage());
		}
		return responseXml;
	}

	/**
	 * 获取航空公司列表
	 * 
	 * @return Map<String, String>
	 */
	public static Map<String, String> getAircompanyList() {
		Date now = new Date();
		if(aircompanyList==null||aircompanyList.size()<=0 || aircompanyUpdateTime == null || aircompanyUpdateTime.before(now))
			aircompanyList = WXProcessHandler.GetAirCompanyMap();
		
		List<String> companyList = new ArrayList<String>();
//		if (aircompanyList != null && aircompanyList.size() > 0&&airCompanyTip.length()<40) {
//			airCompanyTip = "系统暂不支持该航空公司的运单轨迹查询与订阅。";
//			，目前指尖货运支持以下航空公司:\n";
//			Set<String> keySet = aircompanyList.keySet();
//			Iterator<String> it = keySet.iterator();
//			while (it.hasNext()) {
//				String code = it.next();
//				String name = aircompanyList.get(code);
//				int count = 0;
//				for (int i = 0; i < companyList.size(); i++) {
//					if (code.compareTo(companyList.get(i)) <= 0) {
//						companyList.add(i, code);
//						count = 1;
//						break;
//					}
//				}
//				if (count == 0) {
//					companyList.add(code);
//				}
//			}
//			for (int i = 0; i < companyList.size(); i++) {
//				String name = aircompanyList.get(companyList.get(i));
//				airCompanyTip += companyList.get(i) + "-" + name + "\n";
//			}
//		}
		return aircompanyList;
	}

	/**
	 * 获取订单信息
	 * 
	 * @param message
	 * @param openid
	 * @param wxid
	 * @return String
	 * @throws Exception
	 */
	public static String GetTraceData(String message, String openid,
			String wxid, String wxMsgId) throws Exception {
		String requestXml = "<eFreightService><ServiceURL>Order</ServiceURL><ServiceAction>DASHBOARDVIEW</ServiceAction><ServiceData><OrderNo>"
				+ message + "</OrderNo></ServiceData></eFreightService>";
		String responseData = HttpHandler.postHttpRequest(
				"http://air.esinotrans.com/eFreightHttpEngine", requestXml);
		String content = GetTraceHtmlData(responseData, wxid, openid, wxMsgId,
				"MARKETTRACE");
		return content;
	}

	/**
	 * 获取订单html
	 * 
	 * @param responseData
	 * @param wxid
	 * @param openid
	 * @return
	 * @throws Exception
	 */
	private static String GetTraceHtmlData(String responseData, String wxid,
			String openid, String wxMsgId, String msgType) throws Exception {
		String htmlData = "";
		String responseXml = "";
		try {
			Document doc = DocumentHelper.parseText(responseData);
			List<Node> tracesNodeList = doc.selectNodes("//OrderStatusGroup");
			if (tracesNodeList.size() == 0) {
				return GetTextMessageDoc(wxid, "您输入的订单号不存在，请确认订单号正确。", openid,
						wxMsgId, msgType, "NOTFOUND");
			}
			String lastestDate = doc.selectSingleNode(
					"//OrderRecentlyStatusTime").getText();
			String lastestStatus = doc
					.selectSingleNode("//OrderRecentlyStatus").getText();
			String code = doc.selectSingleNode("//OrderNo").getText();
			String listhtml = "";
			SimpleDateFormat longformat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat shortformat = new SimpleDateFormat("MM-dd");
			htmlData = "<p style=\"padding:0px;min-height:1.5em;color:#3e3e3e;\">最新订单轨迹状态</p><p style=\"padding:0px;min-height:1.5em;color:#3e3e3e;\"><span style=\"font-size:10px;color:#a5a5a5;\">"
					+ lastestDate
					+ "</span></p><p style=\"padding:0px;min-height:1.5em;\"><span style=\"color:#76923c;\">"
					+ lastestStatus + "</span></p><p><br /></p>";
			for (int i = 0; i < tracesNodeList.size(); i++) {
				Node traceNode = tracesNodeList.get(i);
				htmlData += "<p><strong><span style=\"font-size:14px;color:#e36c09;text-decoration:underline;\">"
						+ traceNode.selectSingleNode("StatusDate").getText()
						+ "</span></strong></p>";
				List<Node> traceDetailNodeList = traceNode
						.selectNodes("OrderStatus");
				for (int j = 0; j < traceDetailNodeList.size(); j++) {
					Node traceDetailNode = traceDetailNodeList.get(j);
					htmlData += "<p><strong><span style=\"color:#7f7f7f;font-size:12px;\"><span style=\"color:#262626;font-size:12px;\">"
							+ traceDetailNode.selectSingleNode("StatusTime")
									.getText()
							+ "</span></span></strong><span style=\"color:#7f7f7f;font-size:12px;\"><span style=\"color:#262626;font-size:12px;\"></span>"
							+ traceDetailNode.selectSingleNode("StatusDesc")
									.getText() + "</span></p>";
				}
				htmlData += "<p><hr style=\"color:#d8d8d8; width:98%;border-style: dashed;\"/></p>";
			}
			htmlData = (new sun.misc.BASE64Encoder()).encode(htmlData
					.getBytes("UTF-8"));
			responseXml = "<eFreightService>"
					+ "<ServiceURL>WXAPIService</ServiceURL>"
					+ "<ServiceAction>SendWXImageAndTextMessage</ServiceAction>"
					+ "<ServiceData>" + "<wxfakeid>"
					+ wxid
					+ "</wxfakeid><openid>"
					+ openid
					+ "</openid><wxMsgId>"
					+ wxMsgId
					+ "</wxMsgId><messageid>10000127</messageid><MsgType>"
					+ msgType
					+ "</MsgType>"
					+ "<ErrorMsgType>NORMAL</ErrorMsgType><messageitem>"
					+ "<title>中外运电商 "
					+ code
					+ "</title>"
					+ "<digest>最新订单轨迹状态: "
					+ lastestDate
					+ " "
					+ lastestStatus
					+ "</digest>"
					+ "<content>"
					+ htmlData
					+ "</content>"
					+ "<fileid>"
					+ PropertiesUtils.readProductValue("", "markettracefileid")
					+ "</fileid>"
					+ "</messageitem>"
					+ "</ServiceData>"
					+ "</eFreightService>";
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("生成轨迹详细信息是出错：WXAPIServlet-->GetTraceHtmlData()"
					+ e.getMessage());
		}
		return responseXml;
	}
	
	/**
	 * 通过wx消息id查询请求起始毫秒数
	 * @param wxMsgId
	 * @return 返回为－1则标示有问题，没查到
	 */
	public static long getAWBMsgTime(String wxMsgId){
		if(requestTimeChains.containsKey(wxMsgId)){//如果存在，从内存中取毫秒数，如果不存在，去数据库中查询
			Date date = requestTimeChains.get(wxMsgId);
			return System.currentTimeMillis()-date.getTime();
		}else{
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
			try {
				String time = "";
				List<String> timeList = sqlMap.queryForList("loadmessagelogbywxmsgid",wxMsgId);
				if(timeList!=null&&timeList.size()>0){
					time = timeList.get(0);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = sdf.parse(time);
					return System.currentTimeMillis()-date.getTime();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public static void removeAWBMsgTime(String wxMsgId){
		if(requestTimeChains.containsKey(wxMsgId)){
			requestTimeChains.remove(wxMsgId);
		}
	}
	
	/**
	 * 判断url是否存在
	 * @param url
	 * @return
	 */
	public static boolean existsUrl(String url) {
        try {
	        	URL airportInfo = new URL(url);
	    		HttpURLConnection.setFollowRedirects(false);
            //到 URL 所引用的远程对象的连接
            HttpURLConnection con = (HttpURLConnection) airportInfo.openConnection();
            /* 设置 URL 请求的方法， GET POST HEAD OPTIONS PUT DELETE TRACE 以上方法之一是合法的，具体取决于协议的限制。*/
			con.setRequestMethod("HEAD");
			System.out.println("return code : "+con.getResponseCode() +"  url : "+url+"   ");
			return con.getResponseCode()==HttpURLConnection.HTTP_OK;
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //从 HTTP 响应消息获取状态码
        catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
        return false;
	}

	public static boolean GetSubStatus(String userid, String mawb) throws Exception {
		System.out.println("=========GetSubStatus");
		Map<String, String> para = new HashMap<String, String>();
		para.put("receiver", userid);
		if(!"".equals(mawb))
		para.put("mawbcode",mawb);
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		List<Map<String,String>> hisList = sqlMap.queryForList("getuserawbhistory",para);
		return hisList.size()>0;
	}
	public static void main (String[] args){
		String url="http://m.eft.cn/meftcn/tact/CAN-ROM.html";
		WXProcessHandler.existsUrl(url);
	}
}
