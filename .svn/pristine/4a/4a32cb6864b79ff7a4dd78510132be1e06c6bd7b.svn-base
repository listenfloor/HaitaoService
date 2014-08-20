package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.model.UserAWBHis;
import com.efreight.subscribe.UserSettings;
import com.efreight.weixin.handler.WXMessageHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 此类是为了接收客户端针对运单的操作纪录
 */
public class UserAWBHisServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(UserAWBHisServlet.class);
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserAWBHisServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *   处理get请求。
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * 接收从客户端发送过来的历史纪录
	 * 其中根据json串中的ops做区分，如果ops存在，将是进行操作，如果不存在，则认为是查询。
	 * 运单纪录保存
	 * 请求：{
			"userid":"woodenwalker",
			"key":"asfkdjbaskdhaskdjhas",  //校验位
			"ops":"0",   //是否取消订阅。0为取消，1为订阅
			"histype":"SUB", //SUB订阅，QUERY查询
			"pushdayofweek":"1,2,3,4,5",//day of week
			"pushtimefrom":"08:00",//推送时间
			"pushtimeto":"17:00",//推送时间
			"awbnum":"999-12345675", //运单号
			"resource":"WEB" //来源：IOS / WEB
		}
		返回：{
			"statue":"success", //返回状态，success 成功 error 失败
			"description":"成功", //描述
		}
	 *
	 *	运单纪录查询
	 * 请求：{
			"userid":"woodenwalker",
			"key":"asfkdjbaskdhaskdjhas",  //校验位
			"awbnum":"999-12345675", //运单号，可以为空，为空表示查询所有，不为空表示查询单条。
		}
		返回：{
			"userid":"woodenwalker",
			"key":"asfkdjbaskdhaskdjhas",  //校验位
			"data":[
					{
						"awbnum":"999-12345675"
					},
					{
						"awbnum":"180-12345675"
					},
					{
						"awbnum":"760-12345675"
					}
				]
			}
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		BufferedReader is = new BufferedReader(new InputStreamReader(
				request.getInputStream(), "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String s = "";
		while ((s = is.readLine()) != null) {
			sb.append(s);
		}
		String responseStr = "";
		System.out.println(sb.toString());
		log.info(sb.toString());//接收json
		JSONObject json = new JSONObject(sb.toString());
		
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		String userid = null;
		String key = null;
		try{
			userid = json.getString("userid").toUpperCase();
			key = json.getString("key");
		}catch(Exception e ){
		}
		//增加用户信息验证
		if(userid==null||"".equals(userid)){
			responseStr = "{\"status\":\"error\",\"description\":\"用户id为空\"}";
		}
		String ops = null;
		try{
			ops = json.getString("ops");
		}catch(Exception e){
		}
		if(ops!=null&&!"".equals(ops)){//表示是保存新纪录
			responseStr = this.saveUserHis(json);
		}else{//查询结果
			try {
				StringBuffer responsesb = new StringBuffer("{ \"userid\":\""+userid+"\", \"key\":\""+key+"\", \"data\":[");
				List<UserAWBHis> hisList = sqlMap.queryForList("getuserawbhis",userid);
				if(hisList!=null&&hisList.size()>0){
					for(int i=0;i<hisList.size();i++){
						UserAWBHis uah = hisList.get(i);
						responsesb.append("{\"awbnum\":\""+uah.getAwbTraceNum()+"\"}");
						if(i!=(hisList.size()-1)){
							responsesb.append(",");
						}
					}
				}
				responsesb.append("]}");
				responseStr = responsesb.toString();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				responseStr = "{\"status\":\"error\",\"description\":\""+e.getMessage()+"\"}";
			}
		}
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		response.getOutputStream().write(responseStr.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	
	private String saveUserHis(JSONObject json){
		String userid = json.getString("userid").toUpperCase();
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String awbtracenum ="";
		try{
			awbtracenum = json.getString("awbnum");
			awbtracenum = awbtracenum.trim();
			Pattern regex = Pattern.compile("^[0-9]{3}\\-[0-9]{8}$");
			Matcher matcher = regex.matcher(awbtracenum);
			if(!matcher.matches()){
				return "{\"status\":\"error\",\"description\":\"运单号有误，请按规则填写，例:999-12345675\"}";
			}
		}catch(Exception e){
			return "{\"status\":\"error\",\"description\":\"运单号有误\"}";
		}
		Map<String,String> map = new HashMap<String,String>();
//		ID
//		OPENID
//		USERNAME
//		MAWBCODE
//		CREATEDATE
//		HISTYPE
//		pushdayofweek
//		PUSHTIMEFROM
//		PUSHTIMETO
//		DATASOURCE
		map.put("openid", userid );
		map.put("histype", json.getString("ops"));
		map.put("datasource", json.getString("resource"));
		map.put("mawbcode", awbtracenum);
		map.put("createdate", format.format(new Date()));
		if(json.getString("pushdayofweek") != null && !"".equals(json.getString("pushdayofweek"))) {
			map.put("pushdayofweek", json.getString("pushdayofweek"));
			map.put("pushtimefrom", json.getString("pushtimefrom"));
			map.put("pushtimeto", json.getString("pushtimeto"));
		}else {
			UserSettings settings = new UserSettings();
			WXUserinfo userinfo = settings.getUserInfo(userid);
			if(userinfo.getPushdayofweek() == null || "".equals(userinfo.getPushdayofweek())) {
				map.put("pushdayofweek", UserSettings.DEFAULTPUSHDAYOFWEEK);
				map.put("pushtimefrom", UserSettings.DEFAULTPUSHTIMEFROM);
				map.put("pushtimeto", UserSettings.DEFAULTPUSHTIMETO);
			}else{
				map.put("pushdayofweek", userinfo.getPushdayofweek());
				map.put("pushtimefrom", userinfo.getPushtimefrom());
				map.put("pushtimeto", userinfo.getPushtimeto());
			}
		}
		try {
			sqlMap.insert("insertuserawbhis", map);
			return "{\"status\":\"success\",\"description\":\"成功\"}";
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "{\"status\":\"error\",\"description\":\""+e.getMessage()+"\"}";
		}
	}
	
}
