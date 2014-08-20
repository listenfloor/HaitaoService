package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
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

import com.efreight.SQL.Employee;
import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.CommUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.weixin.handler.WXMessageHandler;
import com.ibatis.sqlmap.client.SqlMapClient;


/**
 * Servlet implementation class WXAPIServlet
 */
public class WXLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WXLoginServlet() {
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
		//登陆
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		Map<String,String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("password", CommUtil.getMD5Str(password));
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		Employee emp = null;
		try {
			emp = (Employee)sqlMap.queryForObject("", map);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(emp!=null){
			request.getSession().setAttribute("emp", emp);
			response.getOutputStream().write("success".getBytes());
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return;
		}else{
			response.getOutputStream().write("用户名或密码错误".getBytes());
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return;
		}
	}

	


}
