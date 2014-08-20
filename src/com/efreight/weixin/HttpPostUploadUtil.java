package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;

import com.efreight.commons.PropertiesUtils;


public class HttpPostUploadUtil {

	private String token=null;
	private Map<String, String> cookies=null;
	
	public HttpPostUploadUtil(String token,Map<String,String> cookies){
		this.token=token;
		this.cookies=cookies;
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
//		Properties systemProperties = System.getProperties();
//		systemProperties.setProperty("http.proxyHost", "192.168.0.152");
//		systemProperties.setProperty("http.proxyPort", "8888");
		String BOUNDARY = "---------------------------123821742118716"; // boundary就是request头和上传文件内容的分隔符
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			// conn.setConnectTimeout(5000);
			// conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			// conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Pragma", "no-cache");
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
			//设置cookies
			String cookiestring = "remember_acct="+PropertiesUtils.readProductValue("", "fromuser")+"; hasWarningUser=1;";
			for (String key : cookies.keySet()) {
				cookiestring += key + "=" + cookies.get(key) + "; ";
			}
			if (cookiestring.length() > 0)
				conn.setRequestProperty("Cookie", cookiestring);
			//设置cookies 完成
			conn.setRequestProperty("Connection", "Keep-Alive");
			
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
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
					Map<String,String> map = new HashMap<String, String>();
					map.put("jpg", "image/jpeg");
					map.put("jpeg", "image/jpeg");
					map.put("png", "image/png");
					map.put("gif", "image/gif");
					map.put("bmp", "image/bmp");
					map.put("mp3", "audio/mpeg");
					map.put("wma", "audio/wma");
					map.put("wav", "audio/wav");
					map.put("amr", "audio/amr");
					map.put("rm", "video/rmvb");
					map.put("rmvb", "video/rmvb");
					map.put("wmv", "video/wmv");
					map.put("avi", "video/avi");
					map.put("mpg", "video/mpg");
					map.put("mpeg", "video/mpeg");
					map.put("mp4", "video/mp4");
					String contentType = map.get(filename.substring(filename.lastIndexOf('.')+1));
//					String contentType = new MimetypesFileTypeMap()
//							.getContentType(file);
//					if (filename.endsWith(".png")) {
//						contentType = "image/png";
//					}
					System.out.println(contentType);
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
			System.out.println("file");
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();
			System.out.println("1111111");
			// 读取返回数据
			StringBuffer strBuf = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "utf-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			System.out.println(res);
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
