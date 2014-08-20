package com.efreight.weixin.process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;
import org.json.JSONObject;

import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;

public class SHIPPINGProcess extends WXProcess {
	public SHIPPINGProcess(Document doc){
		try {
			this.doc = doc;
			this.openId = doc.selectSingleNode("//FromUserName").getText();
			String messagetype = doc.selectSingleNode("//MsgType").getText();
			if(messagetype.equalsIgnoreCase("voice"))
				this.message = doc.selectSingleNode("//Recognition").getText().toUpperCase();
			else
				this.message = doc.selectSingleNode("//Content").getText().toUpperCase();
			this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
		}catch(Exception e) {
			
		}
	}
	@Override
	public String snycProcess() {
		new Thread(new WXMessageLogHelper(doc, false, "true", "SHIPPING")).start();
		String wxfakeid = null;
		try {
			wxfakeid = WXInfoDownloader.userWithOpenId.get(openId).fakeid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		URL httpUrl;
		try {
			httpUrl = new URL("http://localhost/GetShippingStatus?blNo="
					+ this.message);
			HttpURLConnection conn = (HttpURLConnection) httpUrl
					.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					(InputStream) conn.getInputStream(), "utf-8"));
			String line = null;
			StringBuilder responseData = new StringBuilder();
			while ((line = in.readLine()) != null) {
				responseData.append(line);
			}
			in.close();
			JSONObject obj = new JSONObject(responseData.toString());
			String response = null;
			
			if(obj.getInt("code")==0) {
				SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy h:mm:ss aaa",Locale.US);
				SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
				Map<String,String> guide = new HashMap<String, String>();
				guide.put("title",this.message + " 的海运最新状态");
				guide.put("content", newFormat.format(format.parse(obj.getJSONArray("body").getJSONObject(0).getString("time"))) + obj.getJSONArray("body").getJSONObject(0).getString("status"));
				guide.put("picUrl","http://m.eft.cn/meftcn/images/shipping.jpg");
				guide.put("url", PropertiesUtils.readProductValue("", "shippingfileurl")+"?blNo=" + this.message);
				messageList.add(guide);
				response = WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId,messageList,this.wxMsgId,"SHIPPING","NORMAL");
				return response;
			}else {
				response = WXProcessHandler.GetTextMessageDoc(wxfakeid, "提单号信息不存在，请重新输入！", this.openId, this.wxMsgId, "SHIPPING", "NORECORD");
			}
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void asnycProcess() {
		// TODO Auto-generated method stub

	}

}
