package com.efreight.weixin.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;

import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXUserinfo;

public class WELCOMEProcess extends WXProcess {

	/**
	 * 用户唯一标示。
	 */
	private String wxfakeid;
	/**
	 * 构造方法。
	 * @param doc
	 */
	public WELCOMEProcess(Document doc){
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}
	
	@Override
	public String snycProcess() {
		WXUserinfo user = WXInfoDownloader.userWithOpenId.get(openId);
		List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
		Map<String,String> wel = new HashMap<String, String>();
		wel.put("title","Hi~ "+user.getNickname()+" 欢迎使用指尖货运，点击进入首页");
		wel.put("content", "欢迎使用指尖货运，点击进入首页");
		wel.put("picUrl","http://192.168.0.232/helpdoc/image/plan_mini.jpg");
		wel.put("url", "http://192.168.0.232/index.html");
		Map<String,String> eplant = new HashMap<String, String>();
		eplant.put("title","运单查询：输入运单号查询轨迹");
		eplant.put("content", "运单查询：输入运单号查询轨迹");
		eplant.put("picUrl","http://192.168.0.232/helpdoc/image/plan_mini_02.jpg");
		eplant.put("url", "http://192.168.0.232/helpdoc/index.html");
		Map<String,String> orderSubcribe = new HashMap<String, String>();
		orderSubcribe.put("title","订阅运单：输入sub＋运单号订阅运单，我们将第一时间为您推送运单轨迹变更");
		orderSubcribe.put("content", "订阅运单：输入sub＋运单号订阅运单，我们将第一时间为您推送运单轨迹变更");
		orderSubcribe.put("picUrl","http://sssssss.ssss.com/image/abc.jpg");
		orderSubcribe.put("url", "http://www.baidu.com");
		Map<String,String> tact = new HashMap<String, String>();
		tact.put("title","运价查询：输入tact＋港口/城市三字码，为您查询从北，上，广，成都始发至目的地运价");
		tact.put("content", "运价查询：输入tact＋港口/城市三字码，为您查询从北，上，广，成都始发至目的地运价");
		tact.put("picUrl","http://sssssss.ssss.com/image/abc.jpg");
		tact.put("url", "http://www.baidu.com");
		messageList.add(wel);
		messageList.add(eplant);
		messageList.add(orderSubcribe);
		messageList.add(tact);
		//判断定时器
		return WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId, messageList,this.wxMsgId,"WELCOME","NORMAL");
	}

	@Override
	public void asnycProcess() {
		// TODO Auto-generated method stub

	}

}
