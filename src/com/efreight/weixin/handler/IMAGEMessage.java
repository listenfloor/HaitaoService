package com.efreight.weixin.handler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;

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
	public String process(){
		String imageUrl = doc.selectSingleNode("//PicUrl").getText();
		String openId= doc.selectSingleNode("//FromUserName").getText();
		String msgId = doc.selectSingleNode("//MsgId").getText();
		if(imageUrl!=null&&!"".equals(imageUrl)){
			URL url;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date nowDate = new Date();
			String now = format.format(nowDate);
			try {
				url = new URL(imageUrl);
				DataInputStream dis = new DataInputStream(url.openStream());
				//设置文件夹
				String dir = imageDir+now.substring(0,4)+"/"+now.substring(5,7)+"/"+now.substring(8,10)+"/";
				File dirf = new File(dir);
				if (!dirf.exists()){
					dirf.mkdirs();
				}
				// 建立一个新的文件
				File f = new File(dir+openId+"_"+msgId+".jpg");
				if(!f.exists()){
					f.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(f);
				byte[] buffer = new byte[1024];
				int length;
				// 开始填充数据
				while ((length = dis.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}
				dis.close();
				fos.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//发送消息日志，image 是图片类型
		new Thread(new WXMessageLogHelper(doc, false, "true","IMAGE")).start();//保存日志
		return null;
	}
}
