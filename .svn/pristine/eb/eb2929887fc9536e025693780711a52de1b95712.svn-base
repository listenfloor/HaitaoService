package com.efreight.weixin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

import com.efreight.SQL.iBatisSQLUtil;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 保存关注日志
 * @author xianan
 *
 */
public class WXSubscribeLogHelper implements Runnable {

	public Document data;

	public WXSubscribeLogHelper(Document doc) {
		this.data = doc;
	}

	@Override
	public void run() {
		try {
			Map<String, String> args = new HashMap<String, String>();
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
//			WXUserinfo userinfo = (WXUserinfo) sqlMap.queryForObject("WXAPIService.getwxuserinfobyopenid", data
//					.selectSingleNode("//FromUserName").getText());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String opdate = format.format(new Date());
			String subscribetype = data.selectSingleNode("//Event").getText();
			Node ticketNode = data.selectSingleNode("//Ticket");
			String key = null;
			key = data.selectSingleNode("//EventKey").getText();
			if(subscribetype.equalsIgnoreCase("SCAN"))
				subscribetype = "scan";
			else if(subscribetype.equalsIgnoreCase("subscribe") && ticketNode != null) {
				subscribetype = "scanforsubscribe";
				key = key.replace("qrscene_", "");
			}
			args.put("openid", data.selectSingleNode("//FromUserName").getText());
//			args.put("wxid", userinfo.wxid);
//			args.put("wxfakeid", userinfo.fakeid);
//			args.put("nickname", userinfo.nickname);
			args.put("datetime", format.format(new Date()));
			args.put("optype", subscribetype);
			args.put("key", key);
			if(ticketNode != null)
				args.put("source",ticketNode.getText());
			sqlMap.update("insertsubscribelog", args);
//			args.put("userstatus", subscribetype);
//			if (subscribetype.equalsIgnoreCase("unsubscribe"))
//				args.put("unsubscribedate", opdate);
//			else
//				args.put("subscribedate", opdate);
//			sqlMap.update("WXAPIService.updateuserinfo", args);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
