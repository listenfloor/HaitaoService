package com.efreight.weixin.publicservice;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import sun.misc.BASE64Encoder;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.CommUtil;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.publicservice.module.ResultModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Servlet implementation class WeixinLogin
 */
@WebServlet("/weixin/authapi")
public class WeixinLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WeixinLogin() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setCharacterEncoding("UTF-8");
		ResultModule result = new ResultModule();
		result.setCode(201);
		result.setMessage("接口调用错误");
		Gson gson = new Gson();
		IOUtils.write(gson.toJson(result), response.getOutputStream(), "utf-8");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		
		String data = null;
		String action = request.getParameter("action");
		if(action != null && action.equalsIgnoreCase("getLoginQRCode"))
			data = this.getLoginQRCode(request,action);
		IOUtils.write(data,response.getOutputStream(),"UTF-8");
		
	}

	private String getLoginQRCode(HttpServletRequest request,String action) {
		String appid = request.getParameter("appid");
		String encrypt = request.getParameter("encrypt");
		String callbackvalue = request.getParameter("callbackvalue");
		String callbackurl = request.getParameter("callbackurl");
		String baseCallbackurl = callbackurl == null? "" : callbackurl;
		ResultModule result = new ResultModule();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			try {
				if (appid == null || "".equals(appid))
					throw new Exception("appid不正确，请重新输入!");
				if (encrypt == null || "".equals(encrypt))
					throw new Exception("参数不完整或不正确，请重新输入!");
			} catch (Exception e) {
				result.setCode(101);
				throw e;
			}
			
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
			try {
				Map<String,String> authMap = (Map<String,String>)sqlMap.queryForObject("getwxloginauth",appid);
				if(authMap == null)
					throw new Exception("appid不存在");
				if(CommUtil.isNullOrEmpty(callbackurl) && CommUtil.isNullOrEmpty((callbackurl = authMap.get("DEFAULTCALLBACKURL"))))
					throw new Exception("回调地址不正确");
				
				StringBuilder sb = new StringBuilder();
				String appkey = authMap.get("APPKEY");
				sb.append(appkey)
				.append("action=")
				.append(action)
				.append("&appid=")
				.append(appid)
				.append("&callbackurl=")
				.append(baseCallbackurl)
				.append("&callbackvalue=")
				.append(callbackvalue);
				System.out.println(sb.toString());
				String baseEncrypt = DigestUtils.md5Hex(sb.toString());
				if(!baseEncrypt.equalsIgnoreCase(encrypt))
					throw new Exception("请求错误，请提供正确的签名");
				
				String ticket = WXInfoDownloader.getTempQrcodeUrl(300,0);
				String imgBase64 = this.getQrcodeData(ticket);
				if(imgBase64 != null && !"".equals(imgBase64)){
					result.setCode(0);
					result.setMessage("获取成功！");
					Map<String, String> resultMap = new HashMap<String, String>();
					resultMap.put("img", imgBase64);
					resultMap.put("token", ticket);
					result.setResult(resultMap);
					result.setExpireon(new Date(new Date().getTime()+300000));
					
				}
					
			} catch (Exception e) {
				result.setCode(102);
				throw e;
//				APPID
//				APPKEY
//				USERNAME
//				DEFAULTCALLBACKURL
//				DESCRIPTION
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
		}
		
		return gson.toJson(result);
	}
	
	private String getQrcodeData(String ticket) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(
				"https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="
						+ URLEncoder.encode(ticket, "UTF-8"));

		HttpResponse imgResponse = client.execute(httpget);
		byte[] imgByte = EntityUtils.toByteArray(imgResponse.getEntity());
		BASE64Encoder encoder = new BASE64Encoder();
		String imgBase64Str = encoder.encode(imgByte);
		return imgBase64Str;
	}

	public static void main(String[] args) {
		String test = "work@efreight.cn" + "ludanwork";
		String result = DigestUtils.sha1Hex(test);
		result = DigestUtils.md5Hex(DigestUtils.md5Hex(test));
		System.out.println(result);
		result = DigestUtils.sha1Hex(result);
		System.out.println(result);
		
	}

}
