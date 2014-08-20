package com.efreight.weixin.menuclickhandler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.CommUtil;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.process.WXProcessHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

public class GetQrcode implements IMenuClickHandler {

	@Override
	public String[] process(String openid, String menuid) {
		String[] response = null;
		try {
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		String ticket = (String)sqlMap.queryForObject("getmarketingqrcodeticket",openid);
		if(ticket == null) {
			ticket = WXInfoDownloader.getPerpetualQrcodeKey("", openid);
			if(ticket == null)
				return null;
			else {
				Map args = new HashMap();
				args.put("ticket", ticket);
				args.put("openid", openid);
				args.put("datetime", new Date());
				sqlMap.insert("createmarketingqrcodeinfo",args);
			}
		}
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpget = new HttpGet("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket);

		HttpResponse httpresponse = client.execute(httpget);
		client.close();
		WXInfoDownloader util = new WXInfoDownloader();
		String mediaid = util.uploadMedia("image", EntityUtils.toByteArray(httpresponse.getEntity()),"temp.jpeg");
		if(mediaid != null) {
			response = new String[]{ WXProcessHandler.GetWXImageMessageCustomApiXML(WXInfoDownloader.userWithOpenId.get(openid).fakeid, mediaid, openid, null, "MARKETINGCLICK", "NORMAL")};
//			response = "{\"touser\":"+openid+",\"msgtype\":\"image\",\"image\":{\"media_id\":\""+mediaid+"\"}}";sdfj
		}else {
//			response = "{\"touser\":\""+openid+"\",\"msgtype\":\"text\",\"text\":{\"content\":\"获取推广二维码出错，请稍后再试！\"}}";
			response =  new String[]{ WXProcessHandler.GetTextMessageDoc(WXInfoDownloader.userWithOpenId.get(openid).fakeid, "获取推广二维码出错，请稍后再试！", openid, null, "MARKETINGCLICK", "ERROR")};
		}
		}catch(Exception e) {
			System.out.println("获取推广QRCODE失败： GetQrcode---》process");
			e.printStackTrace();
		}
		return response;
	}

	
	public static void main(String[] args) throws Exception{
		
//		CloseableHttpClient client = HttpClients.createDefault();
//		HttpPost post = new HttpPost("http://localhost:8080/WeixinService/WXAPIServlet");
//		HttpEntity entity = new StringEntity("<xml><ToUserName><![CDATA[gh_50738c3e80e3]]></ToUserName><FromUserName><![CDATA[oYAngjl_DW2T9CV6r5YYN5cwXlTU]]></FromUserName><CreateTime>1387352772</CreateTime><MsgType><![CDATA[event]]></MsgType><Event><![CDATA[CLICK]]></Event><EventKey><![CDATA[MENU_GETQRCODE_CLICK]]></EventKey></xml>");
//		post.setEntity(entity);
//		HttpResponse response = client.execute(post);
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://localhost:8080/WeixinService/weixin/authapi");
//		HttpEntity entity = new StringEntity("{\"awbcode\":\"999-11111111\",\"content\":\"content\"}","utf-8");
		 List <NameValuePair> nvps = new ArrayList <NameValuePair>();
         nvps.add(new BasicNameValuePair("action", "getLoginQRCode"));
         nvps.add(new BasicNameValuePair("appid", "e86c9fd1d7af9b2b6745bbe1fbc74d65"));
         nvps.add(new BasicNameValuePair("callbackurl", ""));
         nvps.add(new BasicNameValuePair("callbackvalue", "test"));
         
         String willencryptStr = "13a411ac1e575374539e9bb08404e5af0b8155c8action=getLoginQRCode&appid=e86c9fd1d7af9b2b6745bbe1fbc74d65&callbackurl=&callbackvalue=test";
         
         nvps.add(new BasicNameValuePair("encrypt", CommUtil.getMD5Str(willencryptStr)));
         post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
//		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		System.out.println(EntityUtils.toString(response.getEntity(),"UTF-8"));
	}
}
