package com.efreight.subscribe;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.RepeatQueryServlet;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.process.WXProcessHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

public class RepeatQueryOperator implements Runnable{

	public static void init() {
		RepeatQueryOperator operator = new RepeatQueryOperator();
		Thread thread = new Thread(operator);
		thread.start();
	}
	
	@Override
	public void run() {
		System.out.println("--------RepeatQueryOperator-------");
		// TODO Auto-generated method stub
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		while(true) {
			try {
				List<Map<String,Object>> list = sqlMap.queryForList("getqueuelist",new Date());
				for (int i = 0; i < list.size(); i++) {
					Map<String,Object> queueitem = list.get(i);
					String response = null;
					try{
						response = this.requerymessage(queueitem);
					
						if(response.equals("·¢ËÍ³É¹¦"))
							this.setmessagestatus(queueitem,sqlMap);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	ID	NUMBER	No		1	
	DATETIME	DATE	Yes		2	
	OPERATIONTYPE	VARCHAR2(20 BYTE)	Yes		3	
	OPERATIONKEY	VARCHAR2(20 BYTE)	Yes		4	
	ADDDATE	DATE	Yes		5	
	ISSENT	VARCHAR2(20 BYTE)	Yes		6	
	USERID	VARCHAR2(20 BYTE)
 */
	private void setmessagestatus(Map<String, Object> queueitem, SqlMapClient sqlMap) {
		try {
			Object settingid = queueitem.get("SETTINGID");
			sqlMap.update("updatetimingfuncqueue", settingid);
			Map<String,Object> result = (Map<String,Object>)sqlMap.queryForObject("gettimingfuncsettingbyid",queueitem.get("SETTINGID"));
			if(result != null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Integer queueid = (Integer)sqlMap.queryForObject("gettimingqueueseq");
				Date now = new Date();
				Map<String,String> setting = new HashMap<String, String>();
				setting.put("WEEKDAYS", result.get("WEEKDAYS").toString());
				setting.put("INTERVALMINUTES", ((BigDecimal)result.get("INTERVALMINUTES")).intValue()+"");
				setting.put("LASTOPERATIONDATE", format.format(now));
				Map<String,Object> args = new HashMap<String, Object>();
				args.put("type", result.get("OPERATIONTYPE"));
				args.put("key", result.get("OPERATIONKEY"));
				args.put("queueid", queueid);
				args.put("userid", result.get("USERID"));
				args.put("lastoperationdate", now);
				args.put("settingid", settingid);
				Date nextAvailableData = RepeatQueryServlet.getNextAvailableDate(setting);
				args.put("nextoperationdate", nextAvailableData);
				sqlMap.insert("inserttimingfuncqueue",args);
				sqlMap.update("updatelastoperationdate",settingid);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String requerymessage(Map<String, Object> queueitem) {
		WXProcessHandler.getAircompanyList();
		String userid = (String)queueitem.get("USERID");
		String openId = "";
		if(userid != null && !userid.equals("undefined"))
			openId = WXInfoDownloader.userWithFakeId.get(userid).getOpenid();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			String data = WXProcessHandler.GetAwbTraceData((String)queueitem.get("OPERATIONKEY"), userid, openId,"","AWBTRACE");
//			data = data.replace("SendWXImageAndTextMessage", "SendWXImageAndTextMessageWithCustomAPI");
			String requestXml = "<eFreightService>"
					+ "<ServiceURL>Subscribe</ServiceURL>"
					+ "<ServiceAction>TRANSACTION</ServiceAction>"
					+ "<ServiceData>" + "<Subscribe>"
					+ "<type>trace</type><target>" + (String)queueitem.get("OPERATIONKEY")
					+ "</target><targettype>MAWB</targettype>" + "<sync>Y"
					+ "</sync><subscriber>" + userid + "</subscriber><wxMsgId></wxMsgId><subscribertype>NONE"
					+ "</subscribertype>"
					+ "<standard_type>3</standard_type><limit_num>0"
					+ "</limit_num><offflag></offflag><systime>"+format.format(new Date())+"</systime></Subscribe>" + "</ServiceData>"
					+ "</eFreightService>";
			String responseData = HttpHandler
					.postHttpRequest(
							PropertiesUtils.readProductValue("", "awbtraceurl"),
							requestXml);
			String serviceXml = null;
			serviceXml = WXProcessHandler.GetAwbTraceHtmlData(responseData, userid, openId, "", "AWBTRACE", false);
			serviceXml = serviceXml.replace("SendWXImageAndTextMessage", "SendTracePushMessageWithTemplateAPI");
			System.out.println(serviceXml);
			WXAPIServiceProcess process = new WXAPIServiceProcess();
			String response = process.process(serviceXml);
			System.out.println(response);
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
