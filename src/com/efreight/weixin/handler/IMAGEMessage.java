package com.efreight.weixin.handler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;

import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.CerInfoEntity;
import com.efreight.weixin.CerInfoManager;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;

/**
 * 处理用户发送图片请求
 * @author xianan
 *
 */
public class IMAGEMessage extends WXMessageHandler {

	//test
//	private static String imageDir = "D:/tomcat/webapps/wximage/";
//	private static String imageDir = "/usr/share/tomcat6/webapps/wximage/";
	/**
	 * 保存用户上传图片至本地路径
	 */
	private static String imageDir = "/datadisk/tomcat/webapps/wximage/";
//	private static String imageDir = "/etc/";
	/**
	 * 唯一构造方法
	 * @param doc 微信请求过来的xml转换成dom4j的Document类型。
	 * @param url 
	 */
	public IMAGEMessage(Document doc,String url){
		this.doc = doc;
		this.url=url;
	}
	
	/**
	 * 保存图片消息 
	 * 目录结构
	 * 年--
	 * 	 月|--
	 *    日|--
	 *    	  |--文件1
	 *    	  |--文件2
	 *    	  |--......	 
	 * @return String
	 */
	public String process()
	  {
	    String imageUrl = this.doc.selectSingleNode("//PicUrl").getText();
	    String openId = this.doc.selectSingleNode("//FromUserName").getText();
	    String msgId = this.doc.selectSingleNode("//MsgId").getText();

	    System.out.println("imageUrl = " + imageUrl);
	    if ((imageUrl != null) && (!"".equals(imageUrl)))
	    {
	      CerInfoEntity cerInfoEntity = CerInfoManager.getInstance().getConfig(openId);
	      if ((cerInfoEntity != null) && (cerInfoEntity.getIsreadytoupdate().booleanValue()))
	      {
	        if (cerInfoEntity.getCertype() == 3)
	        {
	          cerInfoEntity.setCertype(4);
	          String dir = imageDir + cerInfoEntity.getNumber() + "/";
	          String name = dir + cerInfoEntity.getNumber() + "_front.jpg";
	          cerInfoEntity.setFrontpath(imageUrl);
	          CerInfoManager.getInstance().setConfig(openId, cerInfoEntity);
	          try {
	            download(imageUrl, dir, name);
	          }
	          catch (Exception e) {
	            e.printStackTrace();
	          }

	          WXInfoDownloader util = new WXInfoDownloader();
	          try {
	            util.SendWXTextMessageWithCustomAPI(openId, "请拍摄或上传身份证反面照片");
	          }
	          catch (Exception e) {
	            e.printStackTrace();
	          }
	          return "";
	        }
	        if (cerInfoEntity.getCertype() == 4)
	        {
	          cerInfoEntity.setIsreadytoupdate(Boolean.valueOf(false));
	          String dir = imageDir + cerInfoEntity.getNumber() + "/";
	          String name = dir + cerInfoEntity.getNumber() + "_back.jpg";
	          cerInfoEntity.setBackpath(imageUrl);
	          CerInfoManager.getInstance().setConfig(openId, cerInfoEntity);
	          try {
	            download(imageUrl, dir, name);
	          }
	          catch (Exception e) {
	            e.printStackTrace();
	          }

	          String requestXml = "<Service><ServiceURL>IdentityInfo</ServiceURL><ServiceAction>IdentityInterface</ServiceAction><ServiceData><IDCard>" + 
	            cerInfoEntity.getNumber() + "</IDCard>" + 
	            "<Forwarder></Forwarder>" + 
	            "<InputHandler>" + cerInfoEntity.getName() + "</InputHandler>" + 
	            "<Filepaths>" + 
	            "<Filepath>" + 
	            "<path>" + cerInfoEntity.getFrontpath() + "</path>" + 
	            "<frontandback>0</frontandback>" + 
	            "</Filepath>" + 
	            "<Filepath>" + 
	            "<path>" + cerInfoEntity.getBackpath() + "</path>" + 
	            "<frontandback>1</frontandback>" + 
	            "</Filepath>" + 
	            "</Filepaths>" + 
	            "</ServiceData>" + 
	            "</Service>";
	          HttpHandler.postHttpRequest(PropertiesUtils.readProductValue("", "cerurl"), requestXml);

	          WXInfoDownloader util = new WXInfoDownloader();
	          try {
	            util.SendWXTextMessageWithCustomAPI(openId, "祝贺" + cerInfoEntity.getName() + " " + cerInfoEntity.getNumber() + " 身份证上传成功");
	          }
	          catch (Exception e) {
	            e.printStackTrace();
	          }
	          return "";
	        }
	      }
	      else
	      {
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	        Date nowDate = new Date();
	        String now = format.format(nowDate);

	        String dir = imageDir + now.substring(0, 4) + "/" + now.substring(5, 7) + "/" + now.substring(8, 10) + "/";
	        String name = dir + openId + "_" + msgId + ".jpg";
	        System.out.println("name = " + name);
	        try
	        {
	          download(imageUrl, dir, name);
	        }
	        catch (Exception e) {
	          e.printStackTrace();
	        }
	      }

	    }

	    new Thread(new WXMessageLogHelper(this.doc, false, "true", "IMAGE")).start();
	    return null;
	  }

	  private void download(String urlString, String path, String filename) throws Exception
	  {
	    File dirf = new File(path);
	    if (!dirf.exists()) {
	      dirf.mkdirs();
	    }

	    File f = new File(filename);
	    if (!f.exists()) {
	      f.createNewFile();
	    }

	    URL url = new URL(urlString);

	    URLConnection con = url.openConnection();

	    InputStream is = con.getInputStream();

	    byte[] bs = new byte[1024];

	    OutputStream os = new FileOutputStream(filename);
	    int len;
	    while ((len = is.read(bs)) != -1)
	    {
	      os.write(bs, 0, len);
	    }

	    os.close();
	    is.close();
	  }
}
