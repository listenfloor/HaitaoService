package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Date;
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

import com.efreight.commons.HttpHandler;
import com.efreight.weixin.handler.WXMessageHandler;


/**
 * Servlet implementation class UserLocationServlet
 * 接受请求坐标请求，返回百度地图。
 */
public class UserLocationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserLocationServlet() {
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
	 *  接收参数 x 和 y 坐标
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String x = request.getParameter("x");//获取微信X坐标。
		String y = request.getParameter("y");//获取微信Y坐标。
		//微信的x，y坐标和百度坐标有区别。相反的坐标。
		StringBuffer sb = new StringBuffer("<!DOCTYPE html><html><head>" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" +
				"<style type=\"text/css\">" +
				"body, html,#allmap {width: 100%;height: 100%;overflow: hidden;margin:0;}" +
				"#l-map{height:100%;width:78%;float:left;border-right:2px solid #bcbcbc;}" +
				"#r-result{height:100%;width:20%;float:left;}" +
				"</style>");
		sb.append("<script type=\"text/javascript\" src=\"http://api.map.baidu.com/api?v=1.5&ak=25E1eefa0fd2170a2bcb535617f08ad3\"></script>" +
				"<script type=\"text/javascript\" src=\"http://developer.baidu.com/map/jsdemo/demo/convertor.js\"></script>" +
				"<title>指尖货运-用户提交坐标</title>" +
				"</head>" +
				"<body>" +
				"<div id=\"allmap\"></div>" +
				"</body>" +
				"</html>");
		sb.append("<script type=\"text/javascript\"> \n");
		sb.append("var ggPoint = new BMap.Point("+y+","+x+"); \n");
		sb.append("var bm = new BMap.Map(\"allmap\"); \n");
		sb.append("bm.centerAndZoom(ggPoint, 15); \n");
		sb.append("bm.addControl(new BMap.NavigationControl()); \n");
		
		sb.append("translateCallback = function (point){ \n");
		sb.append("var marker = new BMap.Marker(point); \n");
		sb.append("bm.addOverlay(marker); \n");
		sb.append("var label = new BMap.Label(\"用户提交位置:\",{offset:new BMap.Size(20,-10)}); \n");
		sb.append("marker.setLabel(label); \n");
		sb.append("bm.setCenter(point);} \n");
		
		sb.append("setTimeout(function(){ \n");
		sb.append("BMap.Convertor.translate(ggPoint,2,translateCallback); \n");
		sb.append("}, 2000); \n");
		sb.append("</script>");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		response.getOutputStream().write(sb.toString().getBytes("utf-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	


}
