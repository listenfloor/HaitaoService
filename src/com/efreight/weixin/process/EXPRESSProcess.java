package com.efreight.weixin.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;


public class EXPRESSProcess extends WXProcess {
	
	public EXPRESSProcess(Document doc){
		try {
			this.doc = doc;
			this.openId = doc.selectSingleNode("//FromUserName").getText();
			String messagetype = doc.selectSingleNode("//MsgType").getText();
			if(messagetype.equalsIgnoreCase("voice"))
				this.message = doc.selectSingleNode("//Recognition").getText();
			else
				this.message = doc.selectSingleNode("//Content").getText();
			this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
		}catch(Exception e) {
			
		}
	}

	@Override
	public String snycProcess() {
		new Thread(new WXMessageLogHelper(doc, false, "true", "EXPRESS")).start();
		String commands[] = this.command.split("\\|");
		String expkey = commands[7].trim();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String companyCode = commands[3];
		String response = this.getCachedData(companyCode,expkey,format);
		JSONObject result;
		if(response != null)
			result = new JSONObject(response);
		else {
			result = new JSONObject(this.getExpressInfo(commands[3],expkey));
			result.put("updatetime", format.format(new Date()));
			result.put("companyname", commands[6]);
			if(result.getString("status").equals("1"))
				this.writeToFile(new File(getFilePath(companyCode, expkey)), result.toString());
		}
		response = createMessageData(commands[6],companyCode, result,expkey);
		
		return response;
	}

	private String createMessageData(String companyName,String companyCode, 
			JSONObject result,String expkey) {
		String wxfakeid = null;
		try {
			wxfakeid = WXInfoDownloader.userWithOpenId.get(openId).fakeid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		String response;
		int status = result.getInt("status");
		if(status == 0 || status == 2) {
			return WXProcessHandler.GetTextMessageDoc(wxfakeid, result.getString("message"), this.openId, this.wxMsgId, "EXPRESS", "NORECORD");
		}
		
		JSONArray dataarr = result.getJSONArray("data");
		JSONObject lastStatus = dataarr.getJSONObject(0);
		List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
		Map<String,String> guide = new HashMap<String, String>();
		guide.put("title",companyName + expkey);
		guide.put("content", lastStatus.getString("time") + " " + lastStatus.getString("context"));
		guide.put("picUrl","http://m.eft.cn/meftcn/images/express-title.jpg");
		guide.put("url", PropertiesUtils.readProductValue("", "expressfileurl")+"?com=" + companyCode + "&no=" + expkey);
		messageList.add(guide);
		response = WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId,messageList,this.wxMsgId,"EXPRESS","NORMAL");
		return response;
	}


	private String getCachedData(String company, String code, SimpleDateFormat format) {
		String path = getFilePath(company, code);
		File dataFile = new File(path);
		
		try {
			if(dataFile.exists()) {
				String fileContent = this.readFromFile(dataFile);
				JSONObject obj = new JSONObject(fileContent);
				
				Date updatetime = format.parse(obj.getString("updatetime"));
				if(new Date().getTime() - updatetime.getTime() < 360000) {
					return obj.toString();
				}
			
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getFilePath(String company, String code) {
		String path = PropertiesUtils.readProductValue("", "apachewwwfolder")+ File.separator + "express-cached" + File.separator + company + File.separator + code + ".data";
		System.out.println(path);
		return path;
	}
	
	

	private String readFromFile(File dataFile) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile),"UTF-8"));
			String cache = null;
			while((cache = reader.readLine()) != null) {
				sb.append(cache);
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	private void writeToFile(File dataFile,String data) {
		try {
			if(!dataFile.getParentFile().exists()) {
				dataFile.getParentFile().mkdirs();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile),"UTF-8"));
			writer.write(data);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void asnycProcess() {
		// TODO Auto-generated method stub
		
	}

	private static String getExpressInfo(String companycode, String key) {
		String result = "";
		if (companycode.toLowerCase().equals("shunfeng")
				|| companycode.toLowerCase().equals("shentong"))
			return getExpressInfoFromHtml(companycode, key);

		try {
			String url = "http://api.kuaidi100.com/api?id=116c9f77d5ec1b6e&com="
					+ companycode + "&nu=" + key + "&show=0&muti=1&order=desc";
			result = getHttpResponse(url);
			System.out.println(companycode + "  " + key + "  " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String getHttpResponse(String url) throws MalformedURLException,
			IOException, UnsupportedEncodingException {

		StringBuffer sb = new StringBuffer();
		URL requestUrl = new URL(url);
		HttpURLConnection httpsConn = (HttpURLConnection) requestUrl
				.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpsConn.getInputStream(), "UTF-8"));
		String cache = "";
		
		while ((cache = reader.readLine()) != null) {
			sb.append(cache);
		}
		return sb.toString();
	}
	private static String getExpressInfoFromHtml(String companycode, String key) {
		String url = "http://wx.kuaidi100.com/queryDetail.php?com="+companycode+"&nu="+key;
		String result = "";
		JSONObject obj = new JSONObject();
		try {
			result = getHttpResponse(url);
			
			Pattern regex = Pattern.compile("<dt>(.*?)</dt>.*?<dd>(.*?)</dd>.*?<span class=\"col3\">(.*?)</span>");
			Matcher matcher = regex.matcher(result);
			obj.put("nu", key);
			obj.put("com", companycode);
			boolean find = false;
			List<JSONObject> data = new ArrayList<JSONObject>();
			while (matcher.find()) {
				find = true;
				JSONObject path = new JSONObject();
				path.put("time", matcher.group(1).toString() + " "
						+ matcher.group(2).toString() + ":");
				path.put("context", matcher.group(3).toString());
				data.add(0, path);

			}

			obj.put("data", data);
			if (find) {
				obj.put("message", "ok");
				obj.put("status", "1");
			} else {
				obj.put("status", "0");
				obj.put("message", "‘Àµ•‘›ŒﬁπÏº£");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj.toString();
	}

	public static void main(String[] args) {
		String path = new File(EXPRESSProcess.class.getResource("/").getPath()).getParentFile().getParent()+ File.separator + "express-cached" + File.separator + "11111" + File.separator + "22222" + ".data";
		
		System.out.println(path);
		JSONObject dhl = new JSONObject(getExpressInfo("auspost","TJQ1000593010820"));
		
		System.out.println(dhl.toString());
		
		
	}
}
