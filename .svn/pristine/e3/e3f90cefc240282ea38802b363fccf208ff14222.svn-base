package com.efreight.weixin.process;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXMessageLogHelper;

public class REGProcess extends WXProcess {
		/**
		 * 构造方法
		 * 
		 * @param doc
		 */
		public REGProcess(Document doc) {
			System.out.println("初始化REGProcess");
			this.doc = doc;
			this.openId = doc.selectSingleNode("//FromUserName").getText();
			this.message = doc.selectSingleNode("//Content").getText();
			this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
		}

	@Override
	public void asnycProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	public String snycProcess() {
		new Thread(new WXMessageLogHelper(doc, false, "true", "REGISTRATION")).start();// 保存日志
		String response = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "中外运电子商务平台推介会-触电，将于2013年7月30日在清华大学大礼堂举行，请<a href=\"http://www.esinotrans.com/campaign/?entry=wxmsg\">点击这里</a>报名");
		// TODO Auto-generated method stub
		
		try {
			new Thread(new WXMessageLogHelper(DocumentHelper.parseText(response), true, "true", "REGISTRATION","NORMAL",this.wxMsgId)).start();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// 保存日志
		return response;
	}

}
