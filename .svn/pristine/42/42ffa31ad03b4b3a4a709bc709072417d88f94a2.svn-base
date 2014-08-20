package com.efreight.weixin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import com.efreight.SQL.Employee;
import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.CommUtil;
import com.efreight.commons.DocumentHelper;
import com.ibatis.sqlmap.client.SqlMapClient;
//import com.transformer.dom.DocumentHelper;

/**
 * Created by code machine
 * 
 * @author TransformerPlugin
 */
/**
 * Servlet implementation class WXAPIService
 * 接收xml处理发送weixin消息。
 */
public class WXAPIService extends HttpServlet {
	
	private String publickey = "sssss";
	private static Logger log = Logger.getLogger(WXAPIService.class);
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}


	
	/**
	 * 接收参数名serviceXml
	 * 接收xml，通过WXAPIServiceProcess 去处理。
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String xml = request.getParameter("serviceXml");
		log.info("the post xml :"+xml);
		String key = request.getParameter("key");
		Employee emp = (Employee)request.getSession().getAttribute("emp");
		//判断是否已登陆
		String returnMessage = "";
		try{
			WXAPIServiceProcess process = new WXAPIServiceProcess();
			returnMessage = process.process(xml);
		}catch(Exception e){
			response.getOutputStream().write("xml解析报错".getBytes());
			response.getOutputStream().flush();
			response.getOutputStream().close();
			log.info("the response is : xml解析报错");
			return;
		}
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/xml;charset=utf-8");
		response.getOutputStream().write(returnMessage.getBytes());
		response.getOutputStream().flush();
		response.getOutputStream().close();
		log.info("the response is : "+returnMessage);
		return;
	}
	
	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(arg0, arg1);
	}

}


