package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.ibatis.sqlmap.client.SqlMapClient;

public class RepeatQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RepeatQueryServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader is = new BufferedReader(new InputStreamReader(
				request.getInputStream(), "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String s = "";
		while ((s = is.readLine()) != null) {
			sb.append(s);
		}
		String responseStr = "";
		JSONObject json = new JSONObject(sb.toString());
		
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		String userid = null;
		String key = null;
		String message = "";
		try{
			userid = json.getString("userid").toUpperCase();
			key = json.getString("key");
		}catch(Exception e ){
		}
		if(userid==null||"".equals(userid)){
			responseStr = "{\"status\":\"error\",\"description\":\"用户id为空\"}";
			
		}
		try{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String ops = json.getString("ops");
			String awbnum = json.getString("awbnum");
			String type = json.getString("type");
			Map<String,Object> args = new HashMap<String, Object>();
			args.put("type", type);
			args.put("key", awbnum);
			args.put("userid", userid);
			if(ops.toUpperCase().equals("SUB")) {
				String weekdays = json.getString("weekdays");
				String intervalminutes = json.getString("intervalminutes");
				String repeattimes = json.getString("repeattimes");
				int times = -1;
				try {
					times = Integer.parseInt(repeattimes);
				} catch (Exception e) {
					// TODO: handle exception
				}
				if(times > 0)
					times --;
				if(Integer.parseInt(intervalminutes) < 240) {
					intervalminutes = "240";
					message = "重复间隔过短，已为您修改为4小时。";
				}
				
				if(times == -1) {
					times = 10;
					message += "无限次重复功能正在调试，暂停使用，将重复10次。";
				}
				String lastoperationdate = json.getString("fromdate");
				Map<String,String> setting = new HashMap<String, String>();
				setting.put("WEEKDAYS", weekdays);
				setting.put("INTERVALMINUTES", intervalminutes);
				setting.put("LASTOPERATIONDATE", lastoperationdate);
				args.put("weekdays", weekdays);
				args.put("intervalminutes", intervalminutes);
				args.put("repeattimes", times);
				args.put("lastoperationdate", lastoperationdate);
				Date nextAvailableData = getNextAvailableDate(setting);
				args.put("nextoperationdate", nextAvailableData);
				try {
					Integer queueid = (Integer)sqlMap.queryForObject("gettimingqueueseq");
					args.put("queueid", queueid);
					Map<String,Object> result = (Map<String,Object>)sqlMap.queryForObject("gettimingfuncsetting",args);
					if(result != null) {
						args.put("settingid",result.get("ID"));
						sqlMap.update("updatetimingfuncsetting",args);
						sqlMap.delete("removetimingfuncqueue",args);
						sqlMap.insert("inserttimingfuncqueue",args);
						responseStr = "{\"status\":\"success\",\"description\":\"已更新您之前的设置,下次自动查询时间："+format.format(nextAvailableData)+"。"+message+"\"}";
					}else{
						Integer settingid = (Integer)sqlMap.queryForObject("gettimingsettingseq");
						args.put("settingid", settingid);
						sqlMap.insert("inserttimingfuncsetting", args);
						sqlMap.insert("inserttimingfuncqueue",args);
						responseStr = "{\"status\":\"success\",\"description\":\"定时查询功能设置成功，下次自动查询时间："+format.format(nextAvailableData)+"。"+message+"\"}";
					}
				} catch (SQLException e) {
					throw e;
				}
	//			,#type#,#key#,SYSDATE,#weekdays#,#intervalminutes#,null,'NO',#userid#,#repeattimes#
			}else if(ops.toUpperCase().equals("QUERY")) {
				List<Map<String,Object>> result = sqlMap.queryForList("gettimingfuncsetting",args);
				if(result != null && result.size() > 0) {
					Map<String,Object> data = result.get(0);
					responseStr = "{\"status\":\"success\",\"isset\":\"yes\"," +
							"\"weekdays\":\""+data.get("WEEKDAYS")+"\","+
							"\"intervalminutes\":\""+data.get("INTERVALMINUTES")+"\","+
							"\"fromdate\":\""+format.format(data.get("LASTOPERATIONDATE"))+"\","+
							"\"repeattimes\":\""+data.get("REPEATTIMES")+"\""+
							"}";
				}else {
					responseStr = "{\"status\":\"success\",\"isset\":\"no\"}";
				}
				System.out.println(responseStr);
			}else if(ops.toUpperCase().equals("DELETE")) {
				int i = sqlMap.update("setsettingexpired",args);
				sqlMap.delete("removetimingfuncqueuebykey",args);
				if(i>0) {
					responseStr = "{\"status\":\"success\",\"message\":\"定时查询设置清除成功。\"}";
				}else{
					responseStr = "{\"status\":\"success\",\"message\":\"定时查询设置清除成功，没有设置被清除。\"}";
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			responseStr = "{\"status\":\"error\",\"description\":\""+e.getMessage()+"\"}";
		}
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		response.getOutputStream().write(responseStr.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}
	
	public static Date getNextAvailableDate(Map<String,String> dateAndTime) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dayOfWeek = dateAndTime.get("WEEKDAYS");

		String intervalminutes = dateAndTime.get("INTERVALMINUTES");
		String lastoperationdate = dateAndTime.get("LASTOPERATIONDATE");
		Date baseDate = new Date();
		Calendar calendar = Calendar.getInstance();
		if(lastoperationdate != null){
			calendar.setTime(format.parse(lastoperationdate));
		}
		else
			calendar.setTime(baseDate);
		while(!dayOfWeek.contains(String.valueOf(calendar.get(Calendar.DAY_OF_WEEK))) || calendar.getTime().getTime() < baseDate.getTime()) {
			calendar.add(Calendar.MINUTE, Integer.parseInt(intervalminutes));
		}
		return calendar.getTime();
	}
	
}
