package com.efreight.weixin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.efreight.commons.PropertiesUtils;

/**
 * Servlet implementation class AuthAPI
 */
@WebServlet("/AuthAPI")
public class AuthAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthAPI() {
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
		// TODO Auto-generated method stub
	}
	
	private void updateToken(String code) throws Exception {
		code = "354c55b50984ce586c5ea8225348fcef";
//		https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
		
		URL myURL = new URL("https://api.weixin.qq.com/sns/oauth2/access_token?appid="+PropertiesUtils.readProductValue("", "AppId")+"&secret="+PropertiesUtils.readProductValue("", "AppSecret")+"&code="+code+"&grant_type=authorization_code");
        HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
        InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
        int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        System.out.println(sb.toString());
	}
	private void getUserList() throws Exception {
//		https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&next_openid=NEXT_OPENID
		String token = this.getAccessToken();
		URL myURL = new URL("https://api.weixin.qq.com/cgi-bin/user/get?access_token="+token+"&next_openid=");
        HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
        InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
        int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        System.out.println(sb.toString());
	}
	private void getUserInfo() throws Exception{
//		https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN?access_token=ACCESS_TOKEN&openid=OPENID
		String token = this.getAccessToken();
		URL myURL = new URL("https://api.weixin.qq.com/cgi-bin/user/info?access_token="+token+"&openid=omQjGjj3AHK1fl-FIASpyQoG-8-U");
        HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
        InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
        int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        
        JSONObject obj = new JSONObject(sb.toString());
        System.out.println(obj.getString("nickname"));
        System.out.println(sb.toString());
	}
	
	private void sendMessage() throws Exception{
		//oDVbtju5KB9eftHSvYLdP8NI2fXs
		//oDVbtjm7a2WIFhH1KxsWc9DxwuW0
		//omQjGji2ryvc9pFo2TRWT5RCoU9M
		String msg = "{\"touser\":\"oDVbtjm7a2WIFhH1KxsWc9DxwuW0\",\"msgtype\":\"text\",\"text\":{\"content\":\"Hello World\"}}";
		String token = this.getAccessToken();
		URL myURL = new URL("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="+token);
        HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
        httpsConn.setDoInput(true);
		httpsConn.setDoOutput(true);
		httpsConn.setRequestMethod("POST");

		httpsConn.connect();
		OutputStream os = httpsConn.getOutputStream();
		os.write(msg.getBytes("UTF-8"));
		os.flush();
		os.close();
        InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
        int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        System.out.println(sb.toString());
	}
	
	
	private String getAccessToken() {
		String accessToken = null;
		try {
        URL myURL = new URL("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+PropertiesUtils.readProductValue("", "AppId")+"&secret="+PropertiesUtils.readProductValue("", "AppSecret"));
        HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
        InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
        int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        System.out.println(sb.toString());
//        {"access_token":"o7-g2tH2cAWSKbvJFDlG1HqcRyxl_3CrwVj0gDmVl5vzeTdQ2IhbHX2_qXrkra7WZDig0_TiJwM7kah5OPo9uZpCO30kP3JaG997181cIN8TWGW-x1yeKO6IUcwOu2d3ub7La_UR-ng4-76x_aI94Q","expires_in":7200}
        JSONObject obj = new JSONObject(sb.toString());
        if(sb.toString().contains("errmsg")){
        	System.out.println("ªÒ»°ACCESS_TOKEN ß∞‹"+ obj.getString("errcode")+":"+obj.getString("errmsg"));
        }else
        	accessToken = obj.getString("access_token");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return accessToken;
	}
	public static void main(String[] args) {
		
		AuthAPI auth = new AuthAPI();
		try {
			auth.getUserInfo();
//			auth.getUserInfo();
//			auth.sendMessage();
//			auth.updateToken(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
