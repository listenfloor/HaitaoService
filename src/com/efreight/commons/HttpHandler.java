package com.efreight.commons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

/**
 * httppost帮助类
 */
public class HttpHandler {

	/**
	 * 固定地址发送xml
	 * @param xmlData
	 * @return String
	 */
	public static String postHttpRequest(String xmlData) {
		String url = "http://127.0.0.1:8080/WeixinService/";
		Document document = null;
		try {
			document = DocumentHelper.parseText(xmlData);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		String targetName = com.efreight.commons.DocumentHelper.getNodeText(
				document, "//ServiceURL");
		return postHttpRequest(url + targetName, xmlData);
	}

	/**
	 * 往某地址发送xml
	 * @param urlstr  
	 * @param xmlData
	 * @return String
	 */
	public static String postHttpRequest(String urlstr, String xmlData) {
		URL url = null;
		HttpURLConnection request = null;
		try {
			url = new URL(urlstr);

			request = (HttpURLConnection) url.openConnection();
			request.setReadTimeout(10000);
			request.setDoOutput(true);

			request.setRequestMethod("POST");
			String requestData = "serviceXml="
					+ URLEncoder.encode(xmlData, "UTF-8");
			request.setRequestProperty("Content-Length",
					String.valueOf(requestData.getBytes("UTF-8").length));
			request.connect();
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					request.getOutputStream(), "UTF-8"));
			out.write(requestData);// 要post的数据，多个以&符号分割
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					(InputStream) request.getInputStream(), "utf-8"));
			String line = null;
			StringBuilder responseData = new StringBuilder();
			while ((line = in.readLine()) != null) {
				responseData.append(line);
			}
			in.close();
			return responseData.toString();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String postXml(String xml){
//		String urlStr = "http://127.0.0.1:8080/WeixinService/WXAPIServlet";
//		String urlStr = "http://192.168.0.232:8080/WeixinService/WXAPIServlet";
//		String urlStr = "http://115.28.35.46/WeixinService/WXAPIServlet";
//		String urlStr = "http://192.168.0.232:8080/WeixinService/UserAWBHisServlet";
		String urlStr = "http://192.168.0.232:8080/NewWeixinService/HalloweenServlet";
		String response = "";
		try {
            URL url = new URL(urlStr);
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Pragma:", "no-cache");
            con.setRequestProperty("Cache-Control", "no-cache");
            con.setRequestProperty("Content-Type", "text/json");
            OutputStreamWriter out = new OutputStreamWriter(con
                    .getOutputStream());    
            System.out.println("urlStr=" + urlStr);
            System.out.println("xmlInfo=" + xml);
            out.write(new String(xml.getBytes("utf-8")));
            out.flush();
            out.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(con
                    .getInputStream()));
            String line = "";
            StringBuffer sb =new StringBuffer();
            for (line = br.readLine(); line != null; line = br.readLine()) {
            		sb.append(line);
                System.out.println(line);
            }
            response = sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return response;
	}
	
}
