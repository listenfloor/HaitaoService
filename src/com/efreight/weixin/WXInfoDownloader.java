package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.PropertiesUtils;
import com.efreight.subscribe.MessagePushOperator;
import com.efreight.subscribe.RepeatQueryOperator;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 微信支持类
 * 
 * @author xianan
 * 
 */
public class WXInfoDownloader extends Thread {

	public static String accessToken;
	public static Date expiredTime;

	public static Map<String, Date> userActiveDate = new HashMap<String, Date>();

	/**
	 * cookie
	 */
	private Map<String, String> cookies = new HashMap<String, String>();
	private static Logger log = Logger.getLogger(WXInfoDownloader.class);
	// private final static String USERNAME = "renyj@efreight.me";
	// private final static String PASSWORD = "eFre1ght@wx";
	/**
	 * 登陆用户名
	 */
	private final static String USERNAME = PropertiesUtils.readProductValue("",
			"username");
	/**
	 * 密码
	 */
	private final static String PASSWORD = PropertiesUtils.readProductValue("",
			"password");
	/**
	 * 登陆url
	 */
	private final static String LOGINURL = "https://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN";
	/**
	 * 首页url
	 */
	private final static String INDEXURL = "https://mp.weixin.qq.com/cgi-bin/home?t=home/index&lang=zh_CN";
	/**
	 * 组信息
	 */
	private static Map<String, String> groupInfo;
	/**
	 * fakeid－用户
	 */
	public static Map<String, WXUserinfo> userWithFakeId;
	/**
	 * openid－用户
	 */
	public static Map<String, WXUserinfo> userWithOpenId;
	private static List<WXUserinfo> updateUser;
	private static String token = "";
	static {
		try {
			WXInfoDownloader.loadUserInfoFromDatabase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		WXInfoDownloader.token = token;
	}

	@Override
	public void run() {
		MessagePushOperator.init();
		RepeatQueryOperator.init();
		while (true) {
			try {
				System.out.println("+++++++++++");

				if (cookies == null || !this.GetIndexPage()) {
					this.loginToWX();
				}
				// this.GetUserList();
				// 定时刷新
				// Map<String, String> tempAirCompMap =
				// WXProcessHandler.GetAirCompanyMap();
				// if (tempAirCompMap != null && tempAirCompMap.size() > 0) {
				// WXProcessHandler.aircompanyList = tempAirCompMap;
				// }
				Thread.sleep(900000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	/**
	 * 从数据库中查询用户信息
	 * 
	 * @throws Exception
	 */
	private static void loadUserInfoFromDatabase() throws Exception {
		if (userWithFakeId == null)
			userWithFakeId = new HashMap<String, WXUserinfo>();
		if (groupInfo == null)
			groupInfo = new HashMap<String, String>();
		if (updateUser == null)
			updateUser = new ArrayList<WXUserinfo>();

		if (userWithOpenId == null)
			userWithOpenId = new HashMap<String, WXUserinfo>();
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		List<WXUserinfo> result = sqlMap.queryForList("loadwxuserinfo");
		for (WXUserinfo wxUserinfo : result) {
			userWithFakeId.put(wxUserinfo.fakeid, wxUserinfo);
			userWithOpenId.put(wxUserinfo.openid, wxUserinfo);
		}
		List<Map<String, String>> groupInfos = sqlMap
				.queryForList("loadwxgroupinfo");
		for (Map<String, String> map : groupInfos) {
			groupInfo.put(map.get("GROUPID"), map.get("GROUPLASTFAKEID"));
			// groupInfo.putAll(map);
		}
		// System.out.println(groupInfo);
	}

	/**
	 * 模拟登陆微信
	 * 
	 * @return
	 * @throws Exception
	 */
	protected boolean loginToWX() throws Exception {
		StringBuilder responseData = new StringBuilder();
		try {
			HttpsURLConnection request = this.GetRequest(LOGINURL, "POST");
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
			e.printStackTrace();
			throw new Exception("登陆微信网站失败：" + e.getMessage());
		}
		String responseStr = responseData.toString();
		int index = responseStr.indexOf("token=");
		token = responseStr.substring(index + 6,
				responseStr.indexOf("\"", index));
		log.info("login responseData : " + responseData);
		return responseData.toString().contains("\"Ret\": 302");
	}

	/**
	 * 更新用户
	 */
	private synchronized static void UpdateUser() {
		for (WXUserinfo wxuser : updateUser) {
			Thread thread = new Thread(wxuser);
			thread.start();
		}
		updateUser.clear();
	}

	/**
	 * 获取cookie放到map中
	 * 
	 * @param request
	 */
	private void GetCookies(HttpsURLConnection request) {
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

	/**
	 * 把cookie放到request里
	 * 
	 * @param request
	 */
	private void SetCookies(HttpsURLConnection request) {
		String cookiestring = "";
		for (String key : cookies.keySet()) {
			if (!key.startsWith("master"))
				cookiestring += key + "=" + cookies.get(key) + "; ";
		}
		if (cookiestring.length() > 0)
			request.setRequestProperty("Cookie", cookiestring);
	}

	/**
	 * 
	 * 访问微信主页
	 * 
	 */
	protected boolean GetIndexPage() throws Exception {
		String data = this.GetHttpResponseString(INDEXURL);
		return data.contains("每日接收消息数");
	}

	/**
	 * 获取用户组里用户。
	 * 
	 * @param groupdata
	 * @return
	 * @throws Exception
	 */
	private synchronized List<WXUserinfo> GetUserListByGroup(String groupdata)
			throws Exception {
		groupdata = groupdata.substring(groupdata.indexOf("friendsList"));
		List<WXUserinfo> result = new ArrayList<WXUserinfo>();
		Pattern regex = Pattern
				.compile("\"id\":(.*?),\"nick_name\":\"(.*?)\",\"remark_name\"");
		// Pattern regex =
		// Pattern.compile("\"fakeId\" : \"(.*?)\",([\\s]*?)\"nickName\": \"(.*?)\",");
		Matcher match = regex.matcher(groupdata);
		// System.out.println("!!!!!!!!!!!!!!!!" + groupdata);
		while (match.find()) {
			String name = match.group(2).replaceAll("<.*?></.*?>", "");
			String id = match.group(1);
			WXUserinfo userinfo = new WXUserinfo();
			userinfo.fakeid = id;
			result.add(userinfo);
			if (userWithFakeId.get(id) == null) {
				userinfo.nickname = name;
				userinfo.wxid = this.GetWXIdByFakeId(id);
				userWithFakeId.put(id, userinfo);
				updateUser.add(userinfo);
			}
			if (updateUser.size() != 0) {
				WXInfoDownloader.UpdateUser();
			}
		}
		return result;

	}

	/**
	 * 通过fakeid获取openid
	 * 
	 * @param fakeid
	 * @return String
	 * @throws Exception
	 */
	public String GetWXIdByFakeId(String fakeid) throws Exception {
		String wxid = null;
		try {
			String requestData = "token=" + token
					+ "&lang=zh_CN&t=ajax-getcontactinfo&fakeid=" + fakeid;
			String responseData = this.GetPostHttpResponseString(
					"https://mp.weixin.qq.com/cgi-bin/getcontactinfo",
					requestData);

			JSONObject json = new JSONObject(responseData);
			wxid = json.getJSONObject("contact_info").getString("user_name");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("获取用户" + fakeid + "的微信ID时出错！");
		}
		return wxid;
	}

	/**
	 * 通过fakeid获取openid
	 * 
	 * @param fakeid
	 * @return String
	 * @throws Exception
	 */
	public Map<String, String> GetWXUserMapByFakeId(String fakeid)
			throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		try {
			String requestData = "token=" + token + "&ajax=1";
			String responseData = this
					.GetPostHttpResponseString(
							"https://mp.weixin.qq.com/cgi-bin/getcontactinfo?t=ajax-getcontactinfo&lang=zh_CN&fakeid="
									+ fakeid, requestData);
			System.out.println(responseData);
			JSONObject json = new JSONObject(responseData)
					.getJSONObject("contact_info");
			if (json.getString("user_name") != null
					&& !"".equals(json.getString("user_name"))) {
				map.put("wxid", json.getString("user_name"));
			}
			if (json.getString("nick_name") != null
					&& !"".equals(json.getString("nick_name"))) {
				map.put("nickname",
						json.getString("nick_name")
								.replaceAll("<.*?</.*?>", ""));
			}
			if (json.getString("country") != null
					&& !"".equals(json.getString("country"))) {
				map.put("country", json.getString("country"));
			}
			if (json.getString("province") != null
					&& !"".equals(json.getString("province"))) {
				map.put("province", json.getString("province"));
			}
			if (json.getString("city") != null
					&& !"".equals(json.getString("city"))) {
				map.put("city", json.getString("city"));
			}
		} catch (Exception e) {
			// e.printStackTrace();
			throw new Exception("获取用户" + fakeid + "时出错！");
		}
		return map;
	}

	/**
	 * 
	 * 获取HttpsURLConnection对象，并设置COOKIE
	 */
	private HttpsURLConnection GetRequest(String urlstring, String requestmethod) {
		HttpsURLConnection request = null;
		try {
			URL url = new URL(urlstring);
			request = (HttpsURLConnection) url.openConnection();
			request.setRequestMethod(requestmethod.toUpperCase());

			request.setDoInput(true);
			request.setDoOutput(true);

			request.setRequestProperty("Host", "mp.weixin.qq.com");
			request.setRequestProperty("Connection", "keep-alive");
			request.setRequestProperty("Cache-Control", "max-age=0");
			request.setRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			request.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
			request.setRequestProperty("Referer", "https://mp.weixin.qq.com/");
			request.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			request.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");

			// request.setRequestProperty("Content-Type",
			// "application/x-www-form-urlencoded; charset=UTF-8");
			// request.setRequestProperty("Accept",
			// "application/json, text/javascript, */*; q=0.01");
			// request.setRequestProperty("Referer",
			// "https://mp.weixin.qq.com/cgi-bin/loginpage?t=wxm-login&lang=zh_CN");
			// request.setRequestProperty("UserAgent",
			// "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.97 Safari/537.22");
			// request.setRequestProperty("Accept-Charset",
			// "utf-8;q=0.7,*;q=0.3");
			// request.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			// request.setRequestProperty("Accept-Encoding",
			// "gzip,deflate,sdch");
			this.SetCookies(request);

			// foreach (Cookie cookie in response.Cookies)
			// {
			// cookieContainer.Add(cookie);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	/**
	 * 直接获取GET请求指定地址的结果
	 * 
	 * @param url
	 * @return HTTP请求的response字符串
	 * @throws Exception
	 *             失败
	 */
	private String GetHttpResponseString(String url) throws Exception {
		HttpsURLConnection request = this.GetRequest(url + "&token=" + token,
				"GET");
		request.setDoOutput(false);
		// request.connect();
		// request.getOutputStream().flush();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				(InputStream) request.getInputStream(), "UTF-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		in.close();
		GetCookies(request);
		return sb.toString();
	}

	private String GetGroupURL(String groupid, int size) {
		// return
		// "https://mp.weixin.qq.com/cgi-bin/contactmanagepage?t=wxm-friend&lang=zh_CN&pagesize="
		// + size
		// + "&pageidx=0&type=0&groupid=" + groupid;
		return "https://mp.weixin.qq.com/cgi-bin/contactmanage?t=user/index&pagesize="
				+ size + "&pageidx=0&type=0&groupid=" + groupid + "&lang=zh_CN";
	}

	/**
	 * md5加密
	 * 
	 * @param str
	 * @return
	 */
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

	/**
	 * 往微信发送文本消息
	 * 
	 * @param fakeid
	 *            用户id
	 * @param messagestr
	 *            消息体
	 * @return String
	 * @throws Exception
	 */
	public String SendWXTextMessage(String fakeid, String messagestr)
			throws Exception {
		if (!this.GetIndexPage())
			this.loginToWX();
		String requestData = "type=1&content="
				+ URLEncoder.encode(messagestr, "UTF-8")
				+ "&error=false&tofakeid=" + fakeid + "&ajax=1&token=" + token;
		String responseData = this
				.GetPostHttpResponseString(
						"https://mp.weixin.qq.com/cgi-bin/singlesend?t=ajax-response&lang=zh_CN",
						requestData);
		log.info(" SendWXTextMessage(String fakeid, String messagestr) response : "
				+ responseData);
		return responseData;
		// if (!responseData.contains("\"msg\":\"ok\"")) {
		// return false;
		// }
		// return true;
	}

	/**
	 * 通过客服接口往微信发送文本消息，如果用户上一条主动消息超过30分钟，则会发送失败
	 * 
	 * @param fakeid
	 *            用户id
	 * @param messagestr
	 *            消息体
	 * @return String
	 * @throws Exception
	 */
	public String SendWXTextMessageWithCustomAPI(String wxid, String messagestr)
			throws Exception {

		String msg = "{\"touser\":\"" + wxid
				+ "\",\"msgtype\":\"text\",\"text\":{\"content\":\""
				+ messagestr + "\"}}";
		String responseData = null;
		String token = this.getAccessToken();
		responseData = this.GetPostResponseString(
				"https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="
						+ token, msg);
		return responseData;

	}
	
	public String SendWXImageMessageWithCustomAPI(String wxid, String messagestr)
			throws Exception {

		String msg = "{\"touser\":\""+wxid+"\",\"msgtype\":\"image\",\"image\":{\"media_id\":\""+messagestr+"\"}}";
		String responseData = null;
		String token = this.getAccessToken();
		responseData = this.GetPostResponseString(
				"https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="
						+ token, msg);
		System.out.println(msg);
		System.out.println(responseData);
		return responseData;

	}
	
	public String SendWXMessageWithCustomAPI(String data) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.getAccessToken());
		HttpEntity entity = new StringEntity(data);
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		String responseStr = EntityUtils.toString(response.getEntity());
		return responseStr;
	}

	/**
	 * 发送文件消息
	 * 
	 * @param fakeid
	 *            用户id
	 * @param type
	 *            文件类型
	 * @param fileid
	 *            文件id
	 * @return String
	 * @throws Exception
	 */
	public String SendWXFileMessage(String fakeid, String type, String fileid)
			throws Exception {
		if (!this.GetIndexPage())
			this.loginToWX();
		String requestData = "type=" + type + "&fid=" + fileid + "&fileid="
				+ fileid + "&error=false&tofakeid=" + fakeid + "&token="
				+ token + "&agax=1";
		String responseData = this
				.GetPostHttpResponseString(
						"https://mp.weixin.qq.com/cgi-bin/singlesend?t=ajax-response&lang=zh_CN&type=2",
						requestData);
		log.info(" SendWXFileMessage(String fakeid, String type ,String fileid) response : "
				+ responseData);
		return responseData;
		// if (!responseData.contains("\"msg\":\"ok\"")) {
		// return false;
		// }
		// return true;
	}

	/**
	 * 发送图文消息
	 * 
	 * @param contents
	 *            消息体
	 * @param wxfakeid
	 *            用户id
	 * @param appmsgid
	 *            图文消息id
	 * @return String
	 * @throws Exception
	 */
	public String SendWXImageAndTextMessage(List<Map<String, String>> contents,
			String wxfakeid, String appmsgid) throws Exception {
		if (!this.GetIndexPage())
			this.loginToWX();
		String requestData = "error=false&ajax=1&count=" + contents.size()
				+ "&AppMsgId=&token=" + token + "&";
		StringBuilder requestDataContent = new StringBuilder();
		for (int i = 0; i < contents.size(); i++) {
			Map<String, String> message = contents.get(i);
			requestDataContent
					.append("title" + i)
					.append("="
							+ URLEncoder.encode(message.get("title"), "UTF-8"))
					.append("&");
			requestDataContent
					.append("digest" + i)
					.append("="
							+ URLEncoder.encode(message.get("digest"), "UTF-8"))
					.append("&");
			requestDataContent
					.append("content" + i)
					.append("="
							+ URLEncoder.encode(message.get("content"), "UTF-8"))
					.append("&");
			requestDataContent.append("fileid" + i)
					.append("=" + message.get("fileid")).append("&");
		}
		requestData += requestDataContent
				.append("preusername=")
				.append(WXInfoDownloader.userWithFakeId.get(wxfakeid).getWxid())
				.toString();

		String responseStr = "";
		try {
			responseStr = this
					.GetPostHttpResponseString(
							"https://mp.weixin.qq.com/cgi-bin/operate_appmsg?sub=preview&t=ajax-appmsg-preview",
							requestData);
			if (responseStr.contains("appMsgId")) {
				Pattern pattern = Pattern.compile("\"appMsgId\":\"(\\w*?)\"");
				Matcher matcher = pattern.matcher(responseStr);
				if (matcher.find()) {
					String code = matcher.group(1);
					// this.DeleteWXImageAndTextMessageByAppMsgId(code);
				}
			}
		} catch (Exception e) {
			responseStr = e.getMessage();
			e.printStackTrace();
		}
		log.info(" SendWXImageAndTextMessage(List<Map<String, String>> contents, String wxfakeid,String appmsgid) response : "
				+ responseStr);
		return responseStr;
		// return responseStr.contains("preview send success");
	}

	public String SendWXMessageWithTemplateAPI(String openid,
			String messagedata, String templateid) throws Exception {

		String msg = "{\"touser\": \"" + openid + "\",\"template_id\": \""
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
			else
				accessToken = null;

		}
		return responseData;
	}

	public String SendWXImageAndTextMessageWithCustomAPI(
			List<Map<String, String>> contents, String openid) throws Exception {
		String msg = "{\"touser\":\"" + openid
				+ "\",\"msgtype\":\"news\",\"news\":{\"articles\": [";

		for (int i = 0; i < contents.size(); i++) {
			Map<String, String> message = contents.get(i);
			if (i != 0)
				msg += ",";
			msg += "{\"title\":\"" + message.get("title")
					+ "\",\"description\":\"" + message.get("digest")
					+ "\",\"url\":\"" + message.get("url") + "\",\"picurl\":\""
					+ message.get("imgurl") + "\"}";
		}
		msg += "]}}";
		System.out.println(msg);
		String responseData = null;
		String token = this.getAccessToken();
		responseData = this.GetPostResponseString(
				"https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="
						+ token, msg);
		log.info(" SendWXTextMessage(String fakeid, String messagestr) response : "
				+ responseData);
		return responseData;
	}

	/**
	 * 通过openid获取用户信息
	 * 
	 * @param openid
	 * @param content
	 * @return WXUserinfo
	 * @throws Exception
	 */
	public WXUserinfo GetWXUserinfoByOpenId(String openid, String content)
			throws Exception {
		if (!this.GetIndexPage())
			this.loginToWX();
		System.out.println("查询微信OPENID");
		String url = this.GetGroupURL("0", 20);
		String groupdata = this.GetHttpResponseString(url);

		List<WXUserinfo> userinfo = this.GetUserListByGroup(groupdata);
		System.out.println("从网页获取了" + userinfo.size() + "个用户");
		for (WXUserinfo wxUserinfo : userinfo) {

			String data = this
					.GetHttpResponseString("https://mp.weixin.qq.com/cgi-bin/singlesendpage?tofakeid="
							+ wxUserinfo.getFakeid()
							+ "&t=message/send&action=index&lang=zh_CN");
			Pattern regex = Pattern
					.compile("\"fakeid\":\"(.*?)\",[\\s\\S]*?\"content\":\"(.*?)"
							+ openid + "(.*?)\",");
			Matcher match = regex.matcher(data);
			if (match.find()) {
				log.info("找到OPENID = " + openid);
				Map<String, String> obj = GetWXUserMapByFakeId(wxUserinfo.fakeid);
				JSONObject userJson = getUserInfoJSON(openid);
				wxUserinfo.wxid = obj.get("wxid");
				wxUserinfo.userstatus = "subscribe";
				wxUserinfo.nickname = obj.get("nickname");
				wxUserinfo.city = obj.get("city");
				wxUserinfo.province = obj.get("province");
				wxUserinfo.country = obj.get("country");
				wxUserinfo.language = userJson.getString("language");
				wxUserinfo.lastupdate = new Date();
				wxUserinfo = userWithFakeId.get(wxUserinfo.fakeid);
				wxUserinfo.openid = openid;
				userWithOpenId.put(openid, wxUserinfo);
				userWithFakeId.put(wxUserinfo.fakeid, wxUserinfo);
				Thread newThread = new Thread(wxUserinfo);
				newThread.run();
				return wxUserinfo;
			}
		}
		return null;
	}

	public JSONObject getUserInfoJSON(String openid) throws Exception {
		JSONObject obj = null;
		// {"subscribe":1,"openid":"oDVbtjm7a2WIFhH1KxsWc9DxwuW0","nickname":"大萝卜","sex":1,"language":"zh_CN","city":"顺义","province":"北京","country":"中国"}
		// WXUserinfo info = WXInfoDownloader.userWithOpenId.get(openid);
		if (userWithOpenId.get(openid) == null) {
			obj = new JSONObject();
			String accessToken = this.getAccessToken();

			String userInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token="
					+ accessToken + "&openid=" + openid;
			String userInfoData = this.GetHttpResponseString(userInfoUrl);
			obj = new JSONObject(userInfoData);
		} else if (userWithOpenId.get(openid).lastupdate == null
				|| (userWithOpenId.get(openid).lastupdate.getTime() + 86400000L) < new Date()
						.getTime()
				|| userWithOpenId.get(openid).language == null
				|| "".equals(userWithOpenId.get(openid).language)) {
			WXUserinfo wxuser = userWithOpenId.get(openid);
			obj = getAndUpdateUserInfo(openid, wxuser);
		} else {
			obj = new JSONObject();
			obj.put("language", userWithOpenId.get(openid).getLanguage());
			System.out.println("从缓存中读取用户信息" + openid);
		}
		return obj;
	}
	
	public WXUserinfo GetWXUserinfoByOpenId(String openid) throws Exception {
		if(openid==null) return null;
		
		if (!this.GetIndexPage())
			this.loginToWX();
		WXUserinfo wxUserinfo = this.getUserInfo(openid);
		return wxUserinfo;
	}
	
	public WXUserinfo getUserInfo(String openid) throws Exception {
		System.out.println("-------------------new user--------------------");
		JSONObject obj = null;
		WXUserinfo wxuser = userWithOpenId.get(openid);
		if (wxuser == null) {
			obj = new JSONObject();
			String accessToken = this.getAccessToken();

			String userInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token="
					+ accessToken + "&openid=" + openid;
			String userInfoData = this.GetHttpResponseString(userInfoUrl);
			System.out.println(userInfoData);
			obj = new JSONObject(userInfoData);
			wxuser = new WXUserinfo();
			wxuser.city = obj.getString("city");
			wxuser.nickname = obj.getString("nickname");
			wxuser.language = obj.getString("language");
			wxuser.province = obj.getString("province");
			wxuser.country = obj.getString("country");
			wxuser.headimg = obj.getString("headimgurl");
			wxuser.openid = openid;
			wxuser.fakeid = openid;
			wxuser.wxid = "";
			wxuser.lastupdate = new Date();
			userWithOpenId.put(openid, wxuser);
		} else if (wxuser.lastupdate == null
				|| (wxuser.lastupdate.getTime() + 86400000L) < new Date()
						.getTime()
				|| userWithOpenId.get(openid).language == null
				|| "".equals(userWithOpenId.get(openid).language)) {
			obj = getAndUpdateUserInfo(openid, wxuser);
		}
		return wxuser;
	}

	private JSONObject getAndUpdateUserInfo(String openid, WXUserinfo wxuser)
			throws Exception {
		JSONObject obj;
		String accessToken = this.getAccessToken();

		String userInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token="
				+ accessToken + "&openid=" + openid;
		String userInfoData = this.GetHttpResponseString(userInfoUrl);
		obj = new JSONObject(userInfoData);
		wxuser.city = obj.getString("city");
		wxuser.nickname = obj.getString("nickname");
		wxuser.language = obj.getString("language");
		wxuser.province = obj.getString("province");
		wxuser.country = obj.getString("country");
		wxuser.headimg = obj.getString("headimgurl");
		wxuser.fakeid = openid;
		wxuser.lastupdate = new Date();
		userWithOpenId.put(openid, wxuser);
		Thread newThread = new Thread(wxuser);
		newThread.run();
		System.out.println("更新用户信息" + openid);
		return obj;
	}

	/**
	 * 根据用户id发送图文消息
	 * 
	 * @param msgid
	 *            图文消息id
	 * @param wxfakeid
	 *            用户id
	 * @return String
	 * @throws Exception
	 */
	public String SendWXImageAndTextMessageWithId(String msgid, String wxfakeid)
			throws Exception {
		if (!this.GetIndexPage())
			this.loginToWX();
		String requestData = "type=10&fid=" + msgid + "&appmsgid=" + msgid
				+ "&error=false&tofakeid=" + wxfakeid + "&ajax=1&token="
				+ token;
		String responseData = this
				.GetPostHttpResponseString(
						"https://mp.weixin.qq.com/cgi-bin/singlesend?t=ajax-response&lang=zh_CN",
						requestData);
		log.info(" SendWXImageAndTextMessageWithId(String msgid,String wxfakeid) response : "
				+ responseData);
		return responseData;
		// return responseData.contains("\"msg\":\"ok\"");
	}

	public String getAccessToken() {
		Date now = new Date();
		if (!isAccessTokenAlive(now)) {
			try {

				String sb = this
						.GetResponseString("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
								+ PropertiesUtils.readProductValue("", "AppId")
								+ "&secret="
								+ PropertiesUtils.readProductValue("",
										"AppSecret"));
				JSONObject obj = new JSONObject(sb.toString());
				if (sb.toString().contains("errmsg")) {
					System.out.println("获取ACCESS_TOKEN失败"
							+ obj.getString("errcode") + ":"
							+ obj.getString("errmsg"));
				} else {
					if(accessToken == null)
						accessToken = "";
					if(expiredTime == null)
						expiredTime = new Date();
					synchronized (accessToken) {
						synchronized (expiredTime) {
							accessToken = obj.getString("access_token");
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(new Date());
							calendar.add(Calendar.SECOND, 7000);
							expiredTime = calendar.getTime();
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return accessToken;
	}

	private boolean isAccessTokenAlive(Date now) {

		if (accessToken == null || accessToken.equals("")
				|| expiredTime.before(now) || expiredTime == null)
			return false;
		else
			return true;
	}

	private String GetPostHttpResponseString(String url, String requestData)
			throws Exception {
		HttpsURLConnection request = this.GetRequest(url, "POST");

		request.setRequestProperty("Content-Length",
				String.valueOf(requestData.getBytes("UTF-8").length));
		request.connect();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				request.getOutputStream(), "UTF-8"));
		out.write(requestData);// 要post的数据，多个以&符号分割
		out.flush();
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				(InputStream) request.getInputStream(), "UTF-8"));
		String line = null;
		StringBuilder responseData = new StringBuilder();
		while ((line = in.readLine()) != null) {
			responseData.append(line);
		}
		in.close();
		// System.out.println("发送请求到URL：" + url+",数据：" + requestData + ",结果：" +
		// responseData.toString());
		log.debug(" GetPostHttpResponseString(String url, String requestData) response : "
				+ responseData);
		return responseData.toString();
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

	private String GetResponseString(String url) throws Exception {
		URL requestUrl = new URL(url);
		HttpsURLConnection httpsConn = (HttpsURLConnection) requestUrl
				.openConnection();
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
	
	public static String getTempQrcodeUrl(int timeout,int code) {
		//Step 1 生成二维码ticket值
		if(code == 0){
			Random rand = new Random();
			code = (int)(rand.nextDouble()*1000000000);
		}
		String requestBody = "{\"expire_seconds\": "+timeout+", \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": "+code+"}}}";
		String result = null;
		try {
			WXInfoDownloader util = new WXInfoDownloader();
			String response = util.GetPostHttpResponseString("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + util.getAccessToken(), requestBody);
			JSONObject obj = new JSONObject(response);
			result = obj.getString("ticket");
//			result = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Step 2 返回二维码链接
		return result;
	}
	
	public static synchronized String getPerpetualQrcodeKey(String scene_id,String remark) throws Exception {
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		if(scene_id != null && "".equals(scene_id)) {
			Map codeinfo = (Map)sqlMap.queryForObject("getqrcodeinfo",scene_id);
			if(codeinfo != null && codeinfo.get("QRCODEKEY") != null)
				return (String)codeinfo.get("QRCODEKEY");
		}
		return createNewPerpetualQrcode(remark);
		
	}

	private static synchronized String createNewPerpetualQrcode(String remark) {
		String ticket = null;
		try {
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
			int code = (Integer)sqlMap.queryForObject("getnextcode");
			String requestBody = "{\"action_name\": \"QR_LIMIT_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": "+code+"}}}";
			WXInfoDownloader util = new WXInfoDownloader();
			String response = util.GetPostHttpResponseString("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + util.getAccessToken(), requestBody);
			ticket = new JSONObject(response).getString("ticket");
			Map args = new HashMap();
			args.put("code", code);
			args.put("remark", remark);
			args.put("datetime", new Date());
			args.put("qrcodekey",ticket);
			sqlMap.insert("insertnewcode",args);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ticket;
	}
	
	
	public String uploadMedia(String type,byte[] dataFile,String fileName) {
		String mediaid = null;
		try {
//			HttpHost proxy = new HttpHost("127.0.0.1", 8888);
//			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
//			CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
			CloseableHttpClient httpclient = HttpClients.createDefault();
		    HttpPost httppost = new HttpPost("http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token="+this.getAccessToken()+"&type="+type);

//		    FileEntity reqEntity = new FileEntity(dataFile, ContentType.APPLICATION_OCTET_STREAM);
		    
		    HttpEntity reqEntity = MultipartEntityBuilder.create()
			       	.addBinaryBody("media", dataFile,ContentType.create("image/jpeg"),fileName)
				.build();
		    httppost.setEntity(reqEntity);
		    System.out.println("executing request " + httppost.getRequestLine());
		    HttpResponse response = httpclient.execute(httppost);
		    HttpEntity resEntity = response.getEntity();
	
		    System.out.println(response.getStatusLine());
		    if (resEntity != null) {
		    	String httpResponse = EntityUtils.toString(resEntity);
		    	JSONObject obj = new JSONObject(httpResponse);
		    	
		    	System.out.println(httpResponse);
		    	mediaid = obj.getString("media_id");
		    }
		    httpclient.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return mediaid;
	}

	public static void main(String[] args) throws Exception {
		
		System.out.println(getPerpetualQrcodeKey("1","test"));
		System.out.println("sdfsdfsdfs");
//		System.out.println(System.nanoTime());
//		WXInfoDownloader d = new WXInfoDownloader();
//		System.out.println(d.getUserInfoJSON("oYAngjl_DW2T9CV6r5YYN5cwXlTU"));
		// System.out.println(d.SendWXMessageWithTemplateAPI("oDVbtjm7a2WIFhH1KxsWc9DxwuW0",
		// "{\"awbcode\":\"999-12345678\",\"date\":\"您订a阅的运单您订阅\",\"summary\":\"的运单您订阅的d运单\",\"cargocode\":\"您订阅的运单您订d阅\",\"datasource\":\"的运单您订阅d的运单\",\"warning\":\"您订阅的运单您订d阅\",\"description\":\"的运单您订阅的d运单\",\"comment1\":\"您订阅的运单您订阅的\",\"comment2\":\"运d单您订阅的d运单您\"}",
		// "SB7BOtWkvlR5x2qBvYdnKjbF71viE_m_31GrUDjiicZsBMHjBJIJBqvIM4TMiVig"));

	}

}
