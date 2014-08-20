package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import com.efreight.SQL.WXUploadFile;
import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.weixin.handler.WXMessageHandler;
import com.ibatis.sqlmap.client.SqlMapClient;


/**
 * Servlet implementation class WXFileUploadSearchServlet
 */
public class WXFileUploadSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WXFileUploadSearchServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String type = request.getParameter("type");
		String pageIndex = request.getParameter("pageIndex");
		String pageSize = request.getParameter("pageSize");
		if(pageSize==null||"".equals(pageSize)){
			pageSize = "10";
		}
		if(pageIndex==null||"".equals(pageIndex)){
			String message = getMessage("false","pageIndex is error","");
	    	response.getOutputStream().write(message.getBytes("utf-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return ;
		}
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		Map<String , String> queryMap = new HashMap<String, String>();
		
		int pindex = 0;
		int psize = 0;
		try{
			pindex = Integer.parseInt(pageIndex);
			psize = pindex+Integer.parseInt(pageSize);
		}catch (Exception e) {
			String message = getMessage("false","pageIndex or pageSize is error","");
	    	response.getOutputStream().write(message.getBytes("utf-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return ;
		}
		queryMap.put("fileType", type);
		queryMap.put("pageIndex", pindex+"");
		queryMap.put("pageSize", psize+"");
		int count;
		try {
			count = (Integer) sqlMap.queryForObject("findwxuploadfilecount", queryMap);
			if(pindex>=count){
				String message = getMessage("false","pageIndex is out of lenght","");
		    	response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return ;
			}
			List<WXUploadFile> fileList = sqlMap.queryForList("loadwxuploadfile", queryMap);
			JSONArray arr = new JSONArray();
			if(fileList!=null&&fileList.size()>0){
				for(WXUploadFile file : fileList){
					JSONObject json = new JSONObject();
					json.put("url",file.getFileUrl()).put("fildename", file.getFileName()).put("fileid", file.getWxFileId());
					arr.put(json);
				}
				String message = getMessage("true","success",arr.toString());
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return ;
			}else{
				String message = getMessage("true","result is null","");
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return ;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getMessage(String status,String message,String result){
		StringBuffer sb = new StringBuffer("{\"status\":\""+status+"\",\"description\":\""+message+"\",\"list\":"+result+"}");
		return sb.toString();
	}

}
