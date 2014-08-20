package com.efreight.weixin;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.PropertiesUtils;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Servlet implementation class WXReportServlet 报表servlet
 */
public class WXReportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String publicKey = "sssss";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WXReportServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response) type：类型，用于区分查询报表类型 startdate：开始时间 enddate：结束时间
	 *      wxfakeid：用户id type有如下几种： 1：subscribe关注和取消关注 2：message发送消息
	 *      3：userlist用户列表 4：activitycount活跃用户 5：requestcount发送请求数统计
	 *      6：hourscount按时段统计 7：typestatistics按类型统计
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String type = request.getParameter("type");
		String startdate = request.getParameter("startdate");
		String enddate = request.getParameter("enddate");
		String wxfakeid = request.getParameter("wxfakeid");
		// String key = request.getParameter("key");
		// if(!key.equals(CommUtil.getMD5Str(publicKey+type+startdate+enddate+wxfakeid))){
		// response.getOutputStream().write("[{\"err\":\"访问错误，type参数为空！\"}]".getBytes("UTF-8"));
		// response.getOutputStream().flush();
		// response.getOutputStream().close();
		// return ;
		// }
		String data = "";
		if (startdate != null && startdate.length() == 8)
			startdate = startdate + "000000";
		else if (startdate != null && startdate.length() == 16)
			startdate = startdate + ":00";
		else if (startdate != null && startdate.length() == 10)
			startdate = startdate + " 00:00:00";

		if (enddate != null && enddate.length() == 8)
			enddate = enddate + "235959";
		else if (enddate != null && enddate.length() == 16)
			enddate = enddate + ":59";
		else if (enddate != null && enddate.length() == 10)
			enddate = enddate + " 23:59:59";

		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		if (type != null && !"".equals(type)) {
			try {
				if (type.equalsIgnoreCase("subscribe")) {
					Map<String, String> requestMap = new HashMap<String, String>();
					// String sql =
					// "select openid,wxid,wxfakeid,nickname,to_char(datetime,'yyyy-mm-dd hh24:mi:ss') as opdate,optype,subscribedate,unsubscribedate from subscribelog";
					// String sql =
					// "select s.openid,i.wxid,i.fakeid,i.nickname,to_char(s.datetime,'yyyy-mm-dd hh24:mi:ss') as opdate,s.optype,s.subscribedate,s.unsubscribedate from subscribelog s left join wxuserinfo i on s.OPENID=i.OPENID";
					if (startdate != null)
						// sql += " where s.datetime>="+startdate ;
						requestMap.put("startdate", startdate);
					if (enddate != null) {
						// if(sql.contains("where"))
						// sql += " and ";
						// sql += " s.datetime <=" + enddate;
						requestMap.put("enddate", enddate);
					}
					// sql += " order by s.datetime desc";
					List<Map<String, String>> result = sqlMap.queryForList("subscribelogbydate", requestMap);
					JSONStringer js = new JSONStringer();
					JSONArray arr = new JSONArray();
					for (Map<String, String> map : result) {
						JSONObject json = new JSONObject();
						json.put("openid", map.get("OPENID")).put("wxid", map.get("WXID"))
								.put("wxfakeid", map.get("FAKEID")).put("nickname", map.get("NICKNAME"))
								.put("datetime", map.get("OPDATE")).put("optype", map.get("OPTYPE"))
								.put("subscribedate", "").put("UNSUBSCRIBEDATE", "").put("key", map.get("KEY"));
						arr.put(json);
					}
					data = arr.toString();
				} else if (type.equalsIgnoreCase("message")) {
					Map<String, String> requestMap = new HashMap<String, String>();
					// String sql =
					// "select to_char(datetime,'yyyy-mm-dd hh24:mi:ss') as datetime,sender,receiver,messagetype,content from messagelog";
					// String sql =
					// "select to_char(m.datetime,'yyyy-mm-dd hh24:mi:ss') as datetime,s.fakeid as sender ,r.fakeid as receiver,m.messagetype,m.content,m.success from messagelog m left join wxuserinfo s on m.sender = s.OPENID left join wxuserinfo r on m.receiver = r.openid";
					if (startdate != null)
						requestMap.put("startdate", startdate);
					// sql += " where m.datetime>="+startdate ;
					if (enddate != null) {
						// if(sql.contains("where"))
						// sql += " and ";
						// sql += " m.datetime <=" + enddate;
						requestMap.put("enddate", enddate);
					}
					if (wxfakeid != null && !"".equals(wxfakeid)) {
						// if(sql.contains("where"))
						// sql += " and";
						// else
						// sql += " where";
						// sql +=
						// " (r.fakeid ='"+wxfakeid+"' or s.fakeid='"+wxfakeid+"')";
						requestMap.put("wxfakeid", wxfakeid);
					}
					// sql += " order by m.datetime desc";

					// pstmt = conn.prepareStatement(sql);
					List<Map> result = sqlMap.queryForList("loadmessagelog", requestMap);
					JSONArray arr = new JSONArray();
					for (Map map : result) {
						JSONObject json = new JSONObject();
						json.put("datetime", map.get("DATETIME"))
								.put("messageid", map.get("MESSAGEID"))
								.put("sender", map.get("SENDER"))
								.put("receiver", map.get("RECEIVER"))
								.put("messagetype", map.get("MESSAGETYPE"))
								.put("content",
										(map.get("CONTENT") != null && !"".equals(map.get("CONTENT"))) ? map
												.get("CONTENT") : "失败的空消息").put("wxmsgid", map.get("WXMSGID"))
								.put("msgcount", map.get("MESSAGECOUNT"));
						String success = (String) map.get("SUCCESS");
						// if(success.equals("true")||success.contains("preview send success")||success.contains("\"msg\":\"ok\"")){
						// json.put("success", "true");
						// }else{
						// json.put("success", "false");
						// }
						json.put("success", success);
						json.put("msgid", map.get("MESSAGEID"));
						String msgType = (String) map.get("MSGTYPE");
						String errorMsgType = (String) map.get("MSGRESULT");
						int issend = ((BigDecimal) map.get("ISSEND")).intValue();
						String send = issend == 1 ? "S" : "R";
						json.put("msgtype",
								PropertiesUtils.readERRORMsgTypeValue("", msgType + "|" + errorMsgType + "|" + send));
						arr.put(json);
					}
					data = arr.toString();
				} else if (type.equalsIgnoreCase("userlist")) {
					// String sql =
					// "select fakeid,nickname,wxid,openid,userstatus from wxuserinfo order by entity_syscode desc";
					List<Map<String, String>> result = sqlMap.queryForList("loaduserlistforreport");
					JSONArray arr = new JSONArray();

					for (Map<String, String> map : result) {
						JSONObject json = new JSONObject();
						json.put("openid", map.get("OPENID")).put("wxid", map.get("WXID"))
								.put("wxfakeid", map.get("FAKEID")).put("nickname", map.get("NICKNAME"))
								.put("userstatus", map.get("USERSTATUS") == null ? "subscribe" : "unsubscribe");
						arr.put(json);
					}
					data = arr.toString();
				} else if (type.equalsIgnoreCase("activitycount")) {// 这里的意思是活跃用户
					Map<String, String> requestMap = new HashMap<String, String>();
					if (startdate != null)
						requestMap.put("startdate", startdate);
					if (enddate != null) {
						requestMap.put("enddate", enddate);
					}
					try {
						int activeUser = (Integer) sqlMap.queryForObject("loadactiveusercount", requestMap);
						int userCount = (Integer) sqlMap.queryForObject("loadwxusercount");
						JSONArray arr = new JSONArray();
						JSONObject json = new JSONObject();
						json.put("status", "success").put("countofusers", userCount).put("activityusers", activeUser)
								.put("inactivityusers", (userCount - activeUser)).put("startdate", startdate)
								.put("enddate", enddate);
						arr.put(json);
						data = arr.toString();
					} catch (Exception e) {
						JSONArray arr = new JSONArray();
						JSONObject json = new JSONObject();
						json.put("status", "error");
						data = arr.toString();
						e.printStackTrace();
					}

				} else if (type.equalsIgnoreCase("requestcount")) {// 发送 请求 数 统计
					Map<String, String> requestMap = new HashMap<String, String>();
					if (startdate != null)
						requestMap.put("startdate", startdate);
					if (enddate != null) {
						requestMap.put("enddate", enddate);
					}
					if (wxfakeid != null && !"".equals(wxfakeid)) {
						requestMap.put("wxfakeid", wxfakeid);
					}
					List<Map<String, String>> result = sqlMap.queryForList("loaduserrequestcount", requestMap);
					JSONArray arr = new JSONArray();
					for (Map<String, String> map : result) {
						JSONObject json = new JSONObject();
						json.put("wxid", map.get("WXID")).put("fakeid", map.get("FAKEID"))
								.put("nickname", map.get("NICKNAME")).put("remark", map.get("REMARK"));
						if (map.get("MESSAGECOUNT") == null || "".equals(map.get("MESSAGECOUNT"))) {
							json.put("messagecount", 0);
						} else {
							json.put("messagecount", map.get("MESSAGECOUNT"));
						}
						arr.put(json);
					}
					data = "{\"status\":\"success\",\"startdate\":\"" + startdate + "\",\"enddate\":\"" + enddate
							+ "\",\"data\":" + arr.toString() + "}";
				} else if (type.equalsIgnoreCase("hourscount")) { // 按时段统计消息
					Map<String, String> requestMap = new HashMap<String, String>();
					if (startdate != null)
						requestMap.put("startdate", startdate.substring(0, 14) + "00:00");
					if (enddate != null) {
						requestMap.put("enddate", enddate);
					}
					List<Map<String, String>> result = sqlMap.queryForList("loadmessagecountbyhours", requestMap);
					JSONArray arr = new JSONArray();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date d1 = format.parse(startdate);
					long l1 = d1.getTime();
					Date d2 = format.parse(enddate);
					long l2 = d2.getTime();
					int hours = (int) ((l2 - l1) % (1000 * 60 * 60) > 0 ? ((l2 - l1) / (1000 * 60 * 60) + 1)
							: ((l2 - l1) / (1000 * 60 * 60)));
					SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH");
					for (int i = 0; i < hours; i++) {
						JSONObject json = new JSONObject();
						Date temp = new Date(d1.getTime() + (1000 * 60 * 60) * i);
						String s = format1.format(temp);
						boolean b = false;
						for (Map<String, String> map : result) {
							String date = (String) map.get("MESSAGEDATE");
							if (date.equals(s)) {
								json.put("messagecount", map.get("MESSAGECOUNT")).put("date", s.substring(0, 10))
										.put("hours", s.substring(11));
								b = true;
							}
						}
						if (b == false) {
							json.put("messagecount", 0).put("date", s.substring(0, 10)).put("hours", s.substring(11));
						}
						arr.put(json);
					}
					data = "{\"status\":\"success\",\"data\":" + arr.toString() + "}";
				} else if (type.equalsIgnoreCase("typestatistics")) {// 按类型查询
					Map<String, String> requestMap = new HashMap<String, String>();
					if (startdate != null)
						requestMap.put("startdate", startdate);
					if (enddate != null) {
						requestMap.put("enddate", enddate);
					}
					if (wxfakeid != null && !"".equals(wxfakeid)) {
						requestMap.put("wxfakeid", wxfakeid);
					}
					List<Map> result = sqlMap.queryForList("loadmessagebytype", requestMap);
					int count = 0;
					JSONArray arr = new JSONArray();
					for (Map map : result) {
						JSONObject json = new JSONObject();
						int messageCount = 0;
						String direction = "发送";
						String msgType = (String) map.get("MSGTYPE");
						String errorMsgType = (String) map.get("MSGRESULT");
						int issend = ((BigDecimal) map.get("ISSEND")).intValue();
						if (msgType != null && "SUBSCRIBE".equals(msgType) && errorMsgType != null
								& "PUSH".equals(errorMsgType)) {
							continue;
						}
						String send = issend == 1 ? "S" : "R";
						try {
							String target = PropertiesUtils.readERRORMsgTypeValue("", msgType + "|" + errorMsgType
									+ "|" + send);
							json.put("messagetype", target != null && !"".equals(target) ? target : "其他");
							BigDecimal mc = (BigDecimal) map.get("MESSAGECOUNT");
							messageCount = mc.intValue();
							count += messageCount;
							BigDecimal dic = (BigDecimal) map.get("ISSEND");
							if (dic == null || dic.intValue() == 0) {
								direction = "接收";
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						// 几种类型判断
						json.put("direction", direction).put("messagecount", messageCount);
						arr.put(json);
					}
					data = "{\"status\":\"success\",\"startdate\":\"" + startdate + "\",\"enddate\":\"" + enddate
							+ "\",\"fakeid\":\"" + (wxfakeid != null && !"".equals(wxfakeid) ? wxfakeid : "")
							+ "\",\"messagecount\":\"" + count + "\",\"data\":" + arr.toString() + "}";
				} else if (type.equalsIgnoreCase("getuserinfowithopenid")) {
					System.out.println("=============================");
					String openid = request.getParameter("openid");
					Map<String, String> map = (Map<String,String>)sqlMap.queryForObject("loaduserlistforreport", openid);

					JSONObject json = new JSONObject();
					json.put("openid", map.get("OPENID")).put("wxid", map.get("WXID"))
							.put("wxfakeid", map.get("FAKEID")).put("nickname", map.get("NICKNAME"))
							.put("userstatus", map.get("USERSTATUS") == null ? "subscribe" : "unsubscribe")
							.put("email", map.get("EMAIL"));
					System.out.println(map.get("EMAIL"));
					data = json.toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}

			response.getOutputStream().write(data.getBytes("UTF-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} else {
			response.getOutputStream().write("[{\"err\":\"访问错误，type参数为空！\"}]".getBytes("UTF-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
		}
	}

	public static void main(String[] args) {
		String s = "{\"FakeId\": \"1003922801\",\"NickName\": \"水清朗声\",\"ReMarkName\": \"\",\"Province\": \"北京\"}";
		JSONObject json = new JSONObject(s);
		System.out.println(json.get("Province"));
	}

}
