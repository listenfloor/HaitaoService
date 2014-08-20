package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.efreight.commons.CommUtil;


/**
 * Servlet implementation class MailServlet
 * 邮件发送servlet。通过post接收。
 */
public class MailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(MailServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MailServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *  接收3个参数：
	 *  receiver：接收者
	 *  subject：标题
	 *  content：邮件内容（html经过base64编码）
	 *  通过MailComponent类发送邮件
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String receiver = request.getParameter("receiver");
		String subject = request.getParameter("subject");
		String content = request.getParameter("content");
		log.info("receiver : "+receiver + " subject : "+subject +" content:"+content);
		String responseStr = "";
		if(receiver == null || "".equals(receiver))
			responseStr = "收件人地址为空!";
		if(subject == null || "".equals(subject))
			subject = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "报表";
//		subject = getFromBASE64(content);
		log.info(subject);
		if(content == null || "".equals(content))
			responseStr = "邮件正文为空!";
		else
			content = CommUtil.getFromBASE64(content);
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		if(!responseStr.equals("")){
			response.getOutputStream().write(responseStr.getBytes("UTF-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return;
		}
			
		MailComponent component  = new MailComponent("smtp.ym.163.com", "report@efreight.cn", "pass@word1");
		try {
			component.smtp(receiver, subject, content, false);
			responseStr = "邮件发送成功";
		} catch (Exception e) {
			responseStr = "邮件发送失败，" + e.getMessage();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.getOutputStream().write(responseStr.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	public static void main(String[] args) throws Exception{
		URL url = new URL("http://192.168.0.232:8080/WeixinService/MailServlet");
		HttpURLConnection request = (HttpURLConnection)url.openConnection();
		String requestXml = "subject=测试&content="+CommUtil.getBASE64("<html><head></head><body><font color='red'>测试！</font></body></html>")+"&receiver=xianan@esinotrans.com";
		request.setDoInput(true);
		request.setDoOutput(true);
		request.setRequestMethod("POST");
		request.connect();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				request.getOutputStream(), "UTF-8"));
		out.write(requestXml);// 要post的数据，多个以&符号分割
		out.flush();
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				(InputStream) request.getInputStream()));
		String line = null;
		StringBuffer responseData = new StringBuffer();
		while ((line = in.readLine()) != null) {
			responseData.append(line);
		}
		in.close();
		System.out.println(responseData.toString());
	}
}
