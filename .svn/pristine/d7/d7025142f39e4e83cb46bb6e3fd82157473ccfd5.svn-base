package com.efreight.weixin.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
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

/**
 * Servlet implementation class Halloween
 */
public class Halloween extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Halloween() {
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
		SqlMapClient ibatis = iBatisSQLUtil.getSqlMapInstance();
		try {
		JSONObject json = new JSONObject(sb.toString());
		String action = json.getString("action");
		
			if(action.toLowerCase().equals("candy")) {
				String openid = json.getString("openid");
				String awbcode = json.getString("awbcode");
				Map<String,String> args = new HashMap<String, String>();
				args.put("openid", openid);
				args.put("awbcode", awbcode);
				String exist = (String)ibatis.queryForObject("isawbexist",args);
				
				ibatis.insert("insertcandy", args);
				responseStr = "{\"success\":\"true\",\"exist\":\"";
				if(exist != null && !"".equals(exist)) {
					responseStr +="true";
				}else
					responseStr += "false";
				
				responseStr += "\"}";
			}else if(action.toLowerCase().equals("ranking")) {
				List<Map<String,Object>> result = ibatis.queryForList("getcandy");
				Integer total = (Integer)ibatis.queryForObject("getcount");
				responseStr = "{\"total\":\""+total+"\",\"rankinglist\":[";
				int i = 0;
				for (Map<String, Object> data : result) {
					if(i != 0)
						responseStr += ",";
					responseStr += "{\"username\":\"";
					responseStr += data.get("NICKNAME");
					responseStr += "\",\"candy\":\"";
					responseStr += String.valueOf(data.get("CANDYCOUNT"));
					responseStr += "\"}";
					i++;
				}
				responseStr += "]}";
			}else if(action.toLowerCase().equals("query")) {
				String openid = json.getString("openid");
				Map<String,String> para = new HashMap<String, String>();
				para.put("openid", openid);
				Map<String,Object> result = (Map<String,Object>)ibatis.queryForObject("getcandyforapp",para);
				Integer total = (Integer)ibatis.queryForObject("getcount");
				String count = "1";
				String position = "100+";
				if(result != null) {
					count = String.valueOf(result.get("CANDYCOUNT"));
					position = String.valueOf(result.get("POSITION"));
				}
				responseStr += "{\"candy\":\""+count+"\",\"position\":\""+position+"\",\"total\":\""+total+"\"}";
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			responseStr = "{\"success\":\"false\",\"result\":\""+e.getMessage()+"\"}";
			e.printStackTrace();
		}
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		response.getOutputStream().write(responseStr.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

}
