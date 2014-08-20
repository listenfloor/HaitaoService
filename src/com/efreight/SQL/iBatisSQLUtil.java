package com.efreight.SQL;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

import com.efreight.weixin.WXUserinfo;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class iBatisSQLUtil {

	private static iBatisSQLUtil client  = new iBatisSQLUtil();
	private static SqlMapClient sqlMap = null;
	private iBatisSQLUtil(){
		
	}
	
	static{
		String resource = "com/efreight/SQL/SQLMapConfig.xml";
		Reader reader;
		try {
			reader = Resources.getResourceAsReader(resource);
			sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static iBatisSQLUtil getInstance(){
		if(client==null){
			client = new iBatisSQLUtil();
		}
		return client;
	}
	
	public static SqlMapClient getSqlMapInstance(){
		if(sqlMap==null){
			String resource = "com/efreight/SQL/SQLMapConfig.xml";
			Reader reader;
			try {
				reader = Resources.getResourceAsReader(resource);
				sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sqlMap;
	}
	
	public static void main(String args[]) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	    //新增记录
		
        
//			int s = (Integer) sqlMap.insert("insertwxuploadfile", entryMap);
//			System.out.println(s); // 打印
			Map<String,String> requestMap = new HashMap<String, String>();
//				requestMap.put("startdate", "2013-05-05 00:00:00");
//				requestMap.put("enddate", "2013-05-06 00:00:00");
				requestMap.put("userid", "woodenwalker");
				requestMap.put("createtime", "2013-06-30 18:23:32");
				requestMap.put("isdel", "1");
				requestMap.put("awbtracenum", "180-12345675");
				requestMap.put("resource", "WEB");
		try{
			    WXUserinfo userinfo  = (WXUserinfo) sqlMap.queryForObject("getwxuserinfobyfakeid", "123154323234");
				sqlMap.insert("insertuserawbhis", requestMap);
				List  activeUser =sqlMap.queryForList("getuserawbhis","woodenwalker");
//			List<WXUploadFile> list = sqlMap.queryForList("loadwxuploadfile",map);
				System.out.println(activeUser);
		} catch (Exception e1) {
			e1.printStackTrace();
        }  
	}
}
