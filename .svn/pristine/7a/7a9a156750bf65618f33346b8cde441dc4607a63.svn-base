package com.efreight.weixin.publicservice;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.efreight.weixin.WXInfoDownloader;

/**
 * Servlet implementation class QRCodeService
 */
@WebServlet("/QRCodeService")
public class QRCodeService extends HttpServlet {
	
	public static Map<String, Date> login = new HashMap<String, Date>();
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QRCodeService() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		String key = request.getParameter("key");
		JSONObject obj = new JSONObject();
		if(action == null || "".equals(action)){
			obj.put("code", "101");
			obj.put("message", "请指定action.");
			IOUtils.write(obj.toString(), response.getOutputStream(), "UTF-8");
			response.getOutputStream().close();
			return;
		}
		
		if(action.equalsIgnoreCase("getaccesstoken")) {
			WXInfoDownloader info = new WXInfoDownloader();
			String token = info.getAccessToken();
			obj.put("code", "0");
			obj.put("message", "获取AccessToken成功");
			obj.put("result", token);
			IOUtils.write(obj.toString(), response.getOutputStream(), "UTF-8");
			response.getOutputStream().close();
		}else if(action.equalsIgnoreCase("getperpetualqrcode")) {
			try {
				String ticket = WXInfoDownloader.getPerpetualQrcodeKey(null, "论坛");
				
				IOUtils.write(ticket, response.getOutputStream(),"UTF-8");
				response.getOutputStream().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(action.equalsIgnoreCase("gettempqrcode")) {
			String timeout = request.getParameter("timeout");
			int timeoutint = 1800;
			if(timeout != null && "".equals(timeout)){
				try {
					timeoutint = Integer.parseInt(timeout);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			String ticket = WXInfoDownloader.getTempQrcodeUrl(timeoutint,0);
			IOUtils.write(ticket, response.getOutputStream(),"UTF-8");
			System.out.println(ticket);
			response.getOutputStream().close();
		}else if(action.equalsIgnoreCase("setwaitingtologin")){
			String token = request.getParameter("token");
			synchronized (login) {
				Date now = new Date();
				login.put(token, new Date(now.getTime() + 1000*300));
				
				for (String loginToken : login.keySet()) {
					if (login.get(loginToken).before(now)) {
						login.remove(loginToken);
					}
				}
			}
			response.getOutputStream().close();
		}else if(action.equalsIgnoreCase("getlogininfo")) {
			
		}
		
			
	}

}
