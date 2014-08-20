package com.efreight.weixin.process;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.MapDistance;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.WXUserinfo;
import com.ibatis.sqlmap.client.SqlMapClient;


public class AIRPROTANDCITYProcess extends WXProcess {

	private static Map<String,String> cityMap = new HashMap<String, String>();
	static{
		cityMap.put("BJS", "39.55`116.24");
		cityMap.put("SHA", "31.14`121.29");
		cityMap.put("CAN", "23.08`113.14");
		cityMap.put("CTU", "30.4`104.04");
	}
	
	/**
	 * 构造方法
	 * @param doc
	 */
	public AIRPROTANDCITYProcess(Document doc){
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}
	
	@Override
	public String snycProcess() {
		//首先查询港口信息，如果没有，直接返回错误提示。
		System.out.println("AIRPORTANDCITY snycProcess");
		new Thread(new WXMessageLogHelper(doc, false, "true", "AIRPORT")).start();// 保存日志
		String responseXml = "";
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wxfakeid==null||wxfakeid.equals("")){
			System.out.println("fakeid 没有查到");
			return null;
		}
		String clearMessage = message.trim().toUpperCase();
		boolean isCity = false;
		System.out.println(PropertiesUtils.readProductValue("", "airportfileurl")+clearMessage+"/"+clearMessage+".html");
		System.out.println(PropertiesUtils.readProductValue("", "cityfileurl")+clearMessage+"/"+clearMessage+".html");
		List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
		if(WXProcessHandler.existsUrl(PropertiesUtils.readProductValue("", "airportfileurl")+clearMessage+"/"+clearMessage+".html")){//机场存在
			System.out.println("airport is ture");
			Map<String,String> airport = new HashMap<String,String>();
			airport.put("title",WeixinI18nUtil.getMessageWithOpenid(openId, "airport_info",new Object[]{clearMessage}));
			airport.put("content", WeixinI18nUtil.getMessageWithOpenid(openId, "airport_info",new Object[]{clearMessage}));
			airport.put("picUrl",PropertiesUtils.readProductValue("", "airportfileurl")+clearMessage+"/"+clearMessage+".png");
			airport.put("url", PropertiesUtils.readProductValue("", "airportfileurl")+clearMessage+"/"+clearMessage+".html");
			messageList.add(airport);
		}else if(WXProcessHandler.existsUrl(PropertiesUtils.readProductValue("", "cityfileurl")+clearMessage+"/"+clearMessage+".html")){//城市存在
			System.out.println("city is ture");
			Map<String,String> city = new HashMap<String,String>();
			city.put("title",WeixinI18nUtil.getMessageWithOpenid(openId, "city_info",new Object[]{clearMessage}));
			city.put("content", WeixinI18nUtil.getMessageWithOpenid(openId, "city_info",new Object[]{clearMessage}));
			city.put("picUrl",PropertiesUtils.readProductValue("", "cityfileurl")+clearMessage+"/"+clearMessage+".png");
			city.put("url", PropertiesUtils.readProductValue("", "cityfileurl")+clearMessage+"/"+clearMessage+".html");
			messageList.add(city);
			isCity = true;
		}else{//都不存在
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,WeixinI18nUtil.getMessageWithOpenid(openId, "airport_notexist",new Object[]{clearMessage}),this.openId,this.wxMsgId,"AIRPORT","NORMAL");
			return responseXml;
		}
		
		WXUserinfo user = WXInfoDownloader.userWithFakeId.get(wxfakeid);
		String x= "39.55";
		String y ="116.24";
		String cityname = "";
		if(user.getProvince()!=null){
			if(user.getProvince().equals("北京")||user.getProvince().equals("上海")||user.getProvince().equals("重庆")||user.getProvince().equals("天津")||user.getProvince().equals("澳门")||user.getProvince().equals("香港")){
				cityname=user.getProvince();
			}else{
				cityname = user.getCity();
			}
		}
		if(cityname!=null&&!"".equals(cityname)){
			SqlMapClient sqlmap = iBatisSQLUtil.getSqlMapInstance();
			try {
				List<Map<String,String>> cityMapList = (List<Map<String, String>>) sqlmap.queryForList("findcitycoordinatesbycityname", cityname);
				if(cityMapList!=null&&cityMapList.size()>0){
					Map<String,String> cityCoordinateMap = cityMapList.get(0);
					x = cityCoordinateMap.get("X");
					y = cityCoordinateMap.get("Y");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		List<String> nearstList = new ArrayList();
		Set<String> keySet = cityMap.keySet();
		Iterator it = cityMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,String> ent = (Entry<String, String>) it.next();
			String key = ent.getKey();
			String[] coordinates = ent.getValue().split("`");
//			File tactFile = new File("/Users/xianan/meftcn/tact/"+key+"-"+clearMessage+".html");
			//判断是否存在tact价格html
			if(WXProcessHandler.existsUrl((isCity ? PropertiesUtils.readProductValue("", "tactcityfileurl") : PropertiesUtils.readProductValue("", "tactfileurl") )+"tact/"+key+"-"+clearMessage+".html")){
				if(nearstList!=null&&nearstList.size()>0){
					String[] city = nearstList.get(0).split("`");
					//city[0]地区 city[1]为距离
					if(Double.parseDouble(city[1])>MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))){
						nearstList.add(0, key+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
					}else{
						nearstList.add(key+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
					}
				}else{
					nearstList.add(key+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
				}
			}
		}
		System.out.println("the tact list : "+nearstList.size());
		if(nearstList!=null&&nearstList.size()>0){
			for(int i=0;i<nearstList.size();i++){
				String[] city = nearstList.get(i).split("`");
				Map<String,String> tactMap = new HashMap<String,String>();
				tactMap.put("title",WeixinI18nUtil.getMessageWithOpenid(openId, "tact_main", new Object[]{ city[0], clearMessage}));
				tactMap.put("content", WeixinI18nUtil.getMessageWithOpenid(openId, "tact_main", new Object[]{ city[0], clearMessage}));
				tactMap.put("picUrl",(isCity ? PropertiesUtils.readProductValue("", "tactcityfileurl") : PropertiesUtils.readProductValue("", "tactfileurl") )+"tactimages/"+city[0]+".jpg");
				tactMap.put("url", (isCity ? PropertiesUtils.readProductValue("", "tactcityfileurl") : PropertiesUtils.readProductValue("", "tactfileurl") )+"tact/"+city[0]+"-"+clearMessage+".html");
				messageList.add(tactMap);
			}
		}
		//返回xml
		responseXml =  WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId, messageList,this.wxMsgId,"AIRPORT","NORMAL");
		
		return responseXml;
	}

	@Override
	public void asnycProcess() {
		// TODO Auto-generated method stub

	}
	
}