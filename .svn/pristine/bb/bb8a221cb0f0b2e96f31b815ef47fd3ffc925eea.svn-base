package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.handler.WXMessageHandler;

/**
 * Servlet implementation class WXAPIServlet
 * 此类功能是接收从微信服务器上发来的请求。
 * 微信回直接推送一个xm的字符串，用utf－8编码。返回也是utf－8编码xml。
 * 具体xml格式需要登陆微信网站查询。
 */
public class WXAPIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(WXAPIServlet.class);
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WXAPIServlet() {
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
		String s = request.getParameter("echostr");

		if (s != null) {
			response.getOutputStream().write(s.getBytes());
			response.getOutputStream().flush();
			response.getOutputStream().close();
			System.out.println(s);
			// TODO Auto-generated method stub
		} else {
			response.setContentType("text/html; charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(
					("请通过正常途径访问此网址10000" + WXInfoDownloader.accessToken).getBytes("UTF-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *  接收从微信服务器上post过来xml字符串，然后通过<MsgType>节点解析出来的类型，分别调用com.efreight.weixin.handler包下面EVENTMessage，IMAGEMessage，LOCATIONMessage，TEXTMessage，VOICEMessage类。
	 *  EVENTMessage：处理关注和取消关注的消息
	 *  IMAGEMessage：是处理用户提交图片的消息
	 *  LOCATIONMessage：是处理用户提交坐标的消息
	 *  TEXTMessage：是处理用户提交文本的消息
	 *  VOICEMessage：是处理用户提交声音的消息
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
		log.info(sb.toString());
		String responsexml ="";
		try {
			Document doc = DocumentHelper.parseText(sb.toString());
			Node msgtypenode = doc.selectSingleNode("//MsgType");
			String msgtype = null;
			String openid= doc.selectSingleNode("//FromUserName").getText();
			Date now = new Date();
			now.setTime(now.getTime()+1400*60*1000);
			WXInfoDownloader.userActiveDate.put(openid, now);
			
//			sendMessage(response,"");
			if (msgtypenode != null)
				msgtype = msgtypenode.getText();
			if (msgtype != null) {
				System.out.println("-----------msgtype------------"+msgtype.toUpperCase());
				if(msgtype.equals("voice")) {
					msgtype = "text";
				}
				Class<WXMessageHandler> target = null;
				try {
					target = (Class<WXMessageHandler>) Class
							.forName("com.efreight.weixin.handler."
									+ msgtype.toUpperCase() + "Message");
					Constructor<?> con = target.getConstructor(Document.class,
							String.class);
					WXMessageHandler handler = (WXMessageHandler) con
							.newInstance(doc, "");
					handler.response = response;
					responsexml = handler.process();
				} catch (Exception e) {
					System.out.println("create Handler error.");
					e.printStackTrace();
				}
				if (responsexml != null && !"".equals(responsexml)) {
					System.out.println("return xml : "+responsexml);
					sendMessage(response, responsexml);
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(HttpServletResponse response,String responsexml) throws IOException,
			UnsupportedEncodingException {
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		response.getOutputStream().write(
				responsexml.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

}
