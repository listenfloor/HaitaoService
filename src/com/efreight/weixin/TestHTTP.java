package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.xml.sax.DocumentHandler;


import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;

public class TestHTTP {

	private final static String USERNAME = "efreight";
	private final static String PASSWORD = "pass@word";
	private final static String LOGINURL = "http://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN";
	private final static String INDEXURL = "http://mp.weixin.qq.com/cgi-bin/indexpage?t=wxm-index&lang=zh_CN";
	private static Map<String, String> groupInfo;
	public static Map<String, WXUserinfo> userWithFakeId;
	public static Map<String, WXUserinfo> userWithWXId;
	private static List<WXUserinfo> updateUser;
	private static String token = "";

	private Map<String, String> cookies = new HashMap<String, String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String xml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>PushSyncTraceResult</ServiceAction><ServiceData><WXUserInfo><openid></openid><wxfakeid>348592560</wxfakeid><awb_code>999-12345675</awb_code><message></message><wxMsgId>5893804013358425634</wxMsgId><isError>0</isError><MsgType>AWBTRACE</MsgType><ErrorMsgType>NORMAL</ErrorMsgType></WXUserInfo></ServiceData></eFreightService>";
//		String xml = "<xml><ToUserName><![CDATA[gh_50738c3e80e3]]></ToUserName><FromUserName><![CDATA[ozBfxjnHkZ0yP7fAuNV0veQWkhmQ]]></FromUserName><CreateTime>1372058576</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[176-22451295]]></Content><MsgId>5892946712116339747</MsgId></xml>";
//		String xml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>PushSyncTraceResult</ServiceAction><ServiceData><WXUserInfo><openid></openid><wxfakeid>348592560</wxfakeid><awb_code>020-20304782</awb_code><message>运单020-20304782:网站暂无轨迹</message><wxMsgId>5896263298847206392</wxMsgId><isError>0</isError><MsgType>AWBTRACE</MsgType><ErrorMsgType>NORMAL</ErrorMsgType></WXUserInfo></ServiceData></eFreightService>";
//		String xml = FileProcess.getTextFile("/Users/xianan/Documents/123");
//		WXAPIServiceProcess s = new WXAPIServiceProcess();
//		String resp = s.process(xml);
		String xml = "{\"action\":\"candy\",\"awbcode\":\"12345\",\"openid\":\"966cbfde 477437c6 bdfe98b7 771cf74f cae5771e 21bdef97 88da3133 7525eb62\"}";
		String resp = HttpHandler.postXml(xml);
//		String resp = HttpHandler.postHttpRequest("http://ditu.google.cn/maps?q=Adelaide+++SA+%2C+AUSTRALIA", xml);
		int count = 0 ;
//		while(count<=0){
//			count ++;
//			String resp = HttpHandler.postHttpRequest("http://127.0.0.1:8080/WeixinService/UserAWBHisServlet", xml);
//			System.out.println("count : "+count + " resp:"+resp);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
////			Document doc = DocumentHelper.parseText(resp);
//		String responseData = HttpHandler.postHttpRequest("http://app.efreight.me/HttpEngine", requestXml);
//		try {
//			Document doc = DocumentHelper.parseText(responseData);
//			String resultData = doc.selectSingleNode("//ResultData").getText();
//			System.out.println(resultData);
//		} catch (DocumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(resp);
	}

	protected boolean loginToWX() throws Exception {
		StringBuilder responseData = new StringBuilder();
		try {
			HttpURLConnection request = this.GetRequest(LOGINURL, "POST");
			String requestXml = "username=" + USERNAME + "&pwd="
					+ CreateMD5(PASSWORD) + "&imgcode=&f=json";
			request.setRequestProperty("Content-Length",
					String.valueOf(requestXml.getBytes().length));

			request.connect();
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					request.getOutputStream(), "UTF-8"));
			out.write(requestXml);// 要post的数据，多个以&符号分割
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					(InputStream) request.getInputStream()));
			String line = null;

			while ((line = in.readLine()) != null) {
				responseData.append(line);
			}
			in.close();
			GetCookies(request);
		} catch (Exception e) {
			throw new Exception("登陆微信网站失败：" + e.getMessage());
		}
		String responseStr = responseData.toString();
		int index = responseStr.indexOf("token=");
		token = responseStr.substring(index + 6,
				responseStr.indexOf("\"", index));

		return responseData.toString().contains("\"Ret\": 302");
	}

	private void GetCookies(HttpURLConnection request) {
		List<String> cookieList = request.getHeaderFields().get("Set-Cookie");
		if (cookieList == null)
			return;
		for (String cookie : cookieList) {
			String cookieKeyAndValue = cookie.split(";")[0];
			int position = cookieKeyAndValue.indexOf("=");
			String key = cookieKeyAndValue.substring(0, position);
			String value = cookieKeyAndValue.substring(position + 1);
			cookies.put(key, value);
		}
	}

	private String CreateMD5(String str) {
		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();

			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}

		return md5StrBuff.toString();
	}

	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	/**
	 * 
	 * 获取HTTPURLCONNECTION对象，并设置COOKIE
	 */
	private HttpURLConnection GetRequest(String urlstring, String requestmethod) {
		HttpURLConnection request = null;
		 Properties systemProperties = System.getProperties();
		 systemProperties.setProperty("http.proxyHost", "127.0.0.1");
		 systemProperties.setProperty("http.proxyPort", "8888");
		try {
			URL url = new URL(urlstring);
			request = (HttpURLConnection) url.openConnection();

			request.setRequestMethod(requestmethod.toUpperCase());

			request.setDoInput(true);
			request.setDoOutput(true);
			request.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded; charset=UTF-8");
			request.setRequestProperty("Accept",
					"application/json, text/javascript, */*; q=0.01");
			request.setRequestProperty(
					"Referer",
					"https://mp.weixin.qq.com/cgi-bin/indexpage?token="
							+ token
							+ "&lang=zh_CN&t=wxm-upload&lang=zh_CN&type=0&fromId=file_from_1341151893625");
			request.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.97 Safari/537.22");
			request.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.3");
			request.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			request.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			this.SetCookies(request);
			// request.connect();
			// foreach (Cookie cookie in response.Cookies)
			// {
			// cookieContainer.Add(cookie);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	private void SetCookies(HttpURLConnection request) {
		String cookiestring = "remember_acct=efreight; hasWarningUser=1;";
		for (String key : cookies.keySet()) {
			cookiestring += key + "=" + cookies.get(key) + "; ";
		}
		if (cookiestring.length() > 0)
			request.setRequestProperty("Cookie", cookiestring);
	}

	/**
	 * 发送文本消息
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String SendWXMessage() throws Exception {
		String wxid = "348592560";
		String message = "12345";
		boolean success = this.SendWXTextMessage(wxid, message);
		// 起线程发log
		return success ? "发送成功" : "发送失败";
	}

	public boolean SendWXTextMessage(String fakeid, String messagestr)
			throws Exception {
		String requestData = "type=1&content="
				+ URLEncoder.encode(messagestr, "UTF-8")
				+ "&error=false&tofakeid=" + fakeid + "&ajax=1&token=" + token;
		String responseData = this
				.GetPostHttpResponseString(
						"https://mp.weixin.qq.com/cgi-bin/singlesend?t=ajax-response&lang=zh_CN",
						requestData);

		if (!responseData.contains("\"msg\":\"ok\"")) {
			return false;
		}
		return true;
	}

	private String GetPostHttpResponseString(String url, String requestData)
			throws Exception {
		HttpURLConnection request = this.GetRequest(url, "POST");

		request.setRequestProperty("Content-Length",
				String.valueOf(requestData.getBytes("UTF-8").length));
		request.connect();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				request.getOutputStream(), "UTF-8"));
		out.write(requestData);// 要post的数据，多个以&符号分割
		out.flush();
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				(InputStream) request.getInputStream()));
		String line = null;
		StringBuilder responseData = new StringBuilder();
		while ((line = in.readLine()) != null) {
			responseData.append(line);
		}
		in.close();
		// System.out.println("发送请求到URL：" + url+",数据：" + requestData + ",结果：" +
		// responseData.toString());
		return responseData.toString();
	}

	/**
	 * 上传图片
	 * 
	 * @param urlStr
	 * @param textMap
	 * @param fileMap
	 * @return String
	 */
	public String formUpload(String urlStr, Map<String, String> textMap,
			Map<String, String> fileMap) {
		String res = "";
		HttpURLConnection conn = null;
		String BOUNDARY = "---------------------------123821742118716"; // boundary就是request头和上传文件内容的分隔符
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			// conn.setConnectTimeout(5000);
			// conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			// conn.setDoInput(true);
			// conn.setUseCaches(false);
			conn.setRequestProperty("Host", "mp.weixin.qq.com");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20100101 Firefox/20.0");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language",
					"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty(
					"Referer",
					"http://mp.weixin.qq.com/cgi-bin/indexpage?token="
							+ token
							+ "&lang=zh_CN&t=wxm-upload&lang=zh_CN&type=0&fromId=file_from_1341151893625");
			this.SetCookies(conn);
			conn.setRequestProperty("Connection", "Keep-Alive");

			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(conn.getOutputStream());

			// text
			if (textMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator iter = textMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					strBuf.append("\r\n").append("--").append(BOUNDARY)
							.append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\""
							+ inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}

			// file
			if (fileMap != null) {
				Iterator iter = fileMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					File file = new File(inputValue);
					String filename = file.getName();
					String contentType = new MimetypesFileTypeMap()
							.getContentType(file);
					if (filename.endsWith(".png")) {
						contentType = "image/png";
					}
					if (contentType == null || contentType.equals("")) {
						contentType = "application/octet-stream";
					}

					StringBuffer strBuf = new StringBuffer();
					strBuf.append("\r\n").append("--").append(BOUNDARY)
							.append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
							+ filename + "\"\r\n");
					strBuf.append("Content-Type: " + contentType + "\r\n\r\n");

					out.write(strBuf.toString().getBytes());

					DataInputStream in = new DataInputStream(
							new FileInputStream(file));
					int bytes = 0;
					byte[] bufferOut = new byte[1024];
					while ((bytes = in.read(bufferOut)) != -1) {
						out.write(bufferOut, 0, bytes);
					}
					in.close();
				}
			}

			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			// 读取返回数据
			StringBuffer strBuf = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "utf-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			reader.close();
			reader = null;
		} catch (Exception e) {
			System.out.println("发送POST请求出错。" + urlStr);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

}