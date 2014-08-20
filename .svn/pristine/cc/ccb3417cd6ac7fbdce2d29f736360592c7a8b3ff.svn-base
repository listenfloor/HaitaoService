package com.efreight.weixin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.efreight.commons.PropertiesUtils;

/**
 * Servlet implementation class CustomMenu
 */
@WebServlet("/CustomMenu")
public class CustomMenu extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CustomMenu() {
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
//		String token  = this.getAccessToken();
//		System.out.println(token);
//		if(token == null){
//			System.out.println("error");
//			return;
//		}
		String token = "6ifgUskw_Wywfw-DdvvC3THoLdU1veEDSvUOKHsLSZ29S99DbPKfzb6IR9MnC-Khwtl6byFOGfP9IsuYgNYsz9S2h0XcIRBH67pb2fAm3JtZUoPDUqp-iJ0GyeA6DvyWfr3CtouscuX3mkifM1nuAA";
		InputStream is = PropertiesUtils.class.getResourceAsStream("CustomMenu.json");
		URL menuUrl = new URL("https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+token);
		HttpsURLConnection httpsConn = (HttpsURLConnection)menuUrl.openConnection();
		httpsConn.setDoInput(true);
		httpsConn.setDoOutput(true);
		httpsConn.setRequestMethod("POST");

		httpsConn.connect();
		OutputStream os = httpsConn.getOutputStream();
		int readInt = is.read();
		while(readInt != -1) {
			os.write(readInt);
			readInt = is.read();
		}
		os.flush();
		os.close();
		InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
		int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        System.out.print(sb.toString());
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
        	System.out.println("获取ACCESS_TOKEN失败"+ obj.getString("errcode")+":"+obj.getString("errmsg"));
        }else
        	accessToken = obj.getString("access_token");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return accessToken;
	}
	
	private void getMenuInfo() throws Exception{
		URL myURL = new URL("https://api.weixin.qq.com/cgi-bin/menu/get?access_token="+this.getAccessToken());
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
        
	}
	
	private void deleteMenuInfo() throws Exception {
		URL myURL = new URL("https://api.weixin.qq.com/cgi-bin/menu/delete?access_token="+this.getAccessToken());
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
	
	private void getUserinfo() throws Exception {
		String token = this.getAccessToken();
		URL userinfoUrl = new URL("https://api.weixin.qq.com/cgi-bin/user/info?access_token="+token+"&openid=omQjGji2ryvc9pFo2TRWT5RCoU9M");
		HttpsURLConnection httpsConn = (HttpsURLConnection) userinfoUrl.openConnection();
        InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
        int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        System.out.println(sb.toString());
	}
	
	private void combineMessage() throws Exception{
		String token  = this.getAccessToken();
		URL menuUrl = new URL("https://api.weixin.qq.com/cgi-bin/message/template/combine?access_token="+token);
		HttpsURLConnection httpsConn = (HttpsURLConnection)menuUrl.openConnection();
		httpsConn.setDoInput(true);
		httpsConn.setDoOutput(true);
		httpsConn.setRequestMethod("POST");

		httpsConn.connect();
		OutputStream os = httpsConn.getOutputStream();
		String data = "{\"message\":\"您订阅的运单{{awbcode.DATA}}有新的轨迹：{{date.DATA}}{{summary.DATA}}{{cargocode.DATA}}{{datasource.DATA}}{{warning.DATA}}{{description.DATA}}{{comment1.DATA}}{{comment2.DATA}}\\n回复\\\"详细\\\"查看最后更新运单的轨迹明细或直接回复运单号查看指定运单的轨迹明细。\",\"data\":{\"awbcode\": \"999-12345675\\n\",\"testcode\":\"2013-08-08 15:15:15\",\"summary\":\"运单已上飞机\",\"cargocode\":\"DEP\",\"datasource\":\"中国国航\"}}";
		System.out.println(data);
		os.write(data.getBytes("UTF-8"));
		os.flush();
		os.close();
		InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(),"UTF-8");
		int respInt = insr.read();
        StringBuffer sb = new StringBuffer();
        while (respInt != -1) {
        	sb.append((char)respInt);
            respInt = insr.read();
        }
        System.out.print(sb.toString());
	}
	
	private String createCityJson() {
		Connection conn = null;
		try {
		 Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		 String str = "jdbc:oracle:thin:@124.205.63.187:1521:orcl";
		 conn = DriverManager.getConnection(str, "esinoair", "123456");
		 String sql = "select ap_code,ap_name from airport order by ap_code asc";
		 Statement stmt = conn.createStatement();
		 ResultSet rs = stmt.executeQuery(sql);
//		 JSONArray array = new JSONArray();
		 JSONObject obj = new JSONObject();
		 while(rs.next()) {

			 obj.put(rs.getString("ap_code"), rs.getString("ap_name"));
			 System.out.println(rs.getString(1));
//			 array.put(obj);
		 }
		 return obj.toString();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public String SendWXMessageWithTemplateAPI(String openid,
			String messagedata, String templateid) throws Exception {

		String msg = "{\"touser\": \"" + openid + "\",\"url\":\"http://www.eft.cn\",\"topcolor\":\"#FF0000\",\"template_id\": \""
				+ templateid + "\",\"data\":" + messagedata + "}";
		String responseData = null;
		boolean success = false;
		int i = 0;
		while (!success && i < 3) {
			i++;
			String token = this.getAccessToken();
			responseData = this.GetPostResponseString(
					"https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="
							+ token, msg);
			JSONObject jo = new JSONObject(responseData);
			int code = jo.getInt("errcode");
			if (code == 0)
				success = true;

		}
		return responseData;
	}
	
	private String GetPostResponseString(String url, String requestData)
			throws Exception {
		URL myURL = new URL(url);
		HttpsURLConnection httpsConn = (HttpsURLConnection) myURL
				.openConnection();
		httpsConn.setDoInput(true);
		httpsConn.setDoOutput(true);
		httpsConn.setRequestMethod("POST");

		httpsConn.connect();
		OutputStream os = httpsConn.getOutputStream();
		os.write(requestData.getBytes("UTF-8"));
		os.flush();
		os.close();
		InputStreamReader insr = new InputStreamReader(
				httpsConn.getInputStream(), "UTF-8");
		int respInt = insr.read();
		StringBuffer sb = new StringBuffer();
		while (respInt != -1) {
			sb.append((char) respInt);
			respInt = insr.read();
		}
		return sb.toString();
	}
	
	private void testI18n() {
		ResourceBundle bundle = ResourceBundle.getBundle("com.efreight.weixin.i18n.weixin");
		String msg = bundle.getString("tip");
		System.out.println(MessageFormat.format(msg, "sdkljf"));
	}

	public static void main(String[] args) {


		CustomMenu cm = new CustomMenu();
		try {
//			cm.getUserinfo();
//			System.out.println(cm.createCityJson());
//			cm.deleteMenuInfo();
			cm.doPost(null, null);
//			cm.getMenuInfo();
//			cm.deleteMenuInfo();
//			cm.combineMessage();
//			System.out.println(cm.SendWXMessageWithTemplateAPI("omQjGji2ryvc9pFo2TRWT5RCoU9M", "{\"awbcode\": {\"value\":\"999-12345675\",\"color\":\"#173177\"}}", "YP_1jqLiV5exjpuFmev5VORRcyQBZJUmjAIKkLh6PiCNqxkqst_S0kgrK-4J4Jpl"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
