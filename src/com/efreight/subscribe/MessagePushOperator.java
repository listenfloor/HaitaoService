package com.efreight.subscribe;

import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import oracle.sql.CLOB;
import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXAPIServiceProcess;
import com.ibatis.sqlmap.client.SqlMapClient;

public class MessagePushOperator implements Runnable{
	public static void init() {
		MessagePushOperator operator = new MessagePushOperator();
		Thread thread = new Thread(operator);
		thread.start();
	}
	public Map<String,String> getPushDayAndTime(String userid,String awbcode,String type) {
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		Map<String, String> para = new HashMap<String, String>();
		para.put("receiver", userid);
		para.put("mawbcode", awbcode);
		try {
			List<Map<String,String>> hisList = sqlMap.queryForList("getuserawbhistory",para);
			if(hisList.size() > 0) {
				return hisList.get(0);
			}
				
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean isTimeAvailable(Map<String,String> pushDayAndTime) {
		if(pushDayAndTime == null)
			return true;
		String dayOfWeek = pushDayAndTime.get("PUSHDAYOFWEEK");
		String timeFrom = pushDayAndTime.get("PUSHTIMEFROM");
		String timeTo = pushDayAndTime.get("PUSHTIMETO");
		Date baseDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(baseDate);
		
		int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
		if(dayOfWeek.contains(String.valueOf(currentDay))) {
			Calendar fromCalendar = Calendar.getInstance();
			fromCalendar.setTime(baseDate);
			fromCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeFrom.split(":")[0]));
			fromCalendar.set(Calendar.MINUTE, Integer.parseInt(timeFrom.split(":")[1]));
			Calendar toCalendar = Calendar.getInstance();
			toCalendar.setTime(baseDate);
			toCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeTo.split(":")[0]));
			toCalendar.set(Calendar.MINUTE, Integer.parseInt(timeTo.split(":")[1]));
			if(calendar.before(toCalendar) && calendar.after(fromCalendar))
				return true;
			
		}
		return false;
	}
	
	public Date getNextAvailableDate(Map<String,String> pushDayAndTime) {
		String dayOfWeek = pushDayAndTime.get("PUSHDAYOFWEEK");
		String timeFrom = pushDayAndTime.get("PUSHTIMEFROM");
		Date baseDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(baseDate);
		int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
		int plusDay = 0;
		System.out.println(currentDay);
		Date result = null;
		while (result == null) {
			result = getAvailableTime(dayOfWeek, timeFrom, calendar, currentDay,plusDay);
			if(currentDay < 8) {
				currentDay ++;
			}else {
				currentDay = 1;
			}
			plusDay ++;
		}

		return result;
	}
	private Date getAvailableTime(String dayOfWeek, String timeFrom,
			Calendar calendar, int currentDay, int plusDay)  {
		System.out.println(currentDay);
		if(dayOfWeek.contains(String.valueOf(currentDay))) {
			calendar.add(Calendar.DAY_OF_MONTH,plusDay);
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeFrom.split(":")[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(timeFrom.split(":")[1]));
			return calendar.getTime();
		}
		return null;
	}
	
	public void addPushInfoToQueue(String awbcode,String type,String content,Date pushtime) {
			CLOB clob = null;
			String query;
			// Initialize statement Object
			PreparedStatement pstmt = null;
			Connection conn = null;
			Connection oracleConnection = null;
			try {
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup(PropertiesUtils.readProductValue("", "jndipath"));
				conn = ds.getConnection();
				DelegatingConnection delegatingConn = (DelegatingConnection) conn;
				oracleConnection = delegatingConn.getInnermostDelegate();
				if (oracleConnection == null)
					oracleConnection = delegatingConn.getMetaData().getConnection();
				oracleConnection = ((DelegatingConnection) oracleConnection)
						.getDelegate();
				if (oracleConnection == null) {
					System.out.println("没有获得连接");
				}

				query = "insert into PUSHQUEUE (ID,PUSHTYPE,MAWBCODE,PUSHDATE,CONTENT,CREATEDATE,ISSEND) values"
						+ " (seq_pushqueue.nextval,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),SYS.XMLTYPE(?),sysdate,?)";
				pstmt = oracleConnection.prepareStatement(query);
				clob = getCLOB(content, oracleConnection);
				pstmt.setObject(1, type);
				pstmt.setObject(2, awbcode);
				pstmt.setObject(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pushtime));
				pstmt.setObject(4, clob);
				pstmt.setObject(5, "false");
				if (pstmt.executeUpdate() == 1) {
					System.out.println("Successfully inserted a Purchase Order");
				}
			} catch (SQLException sqlexp) {
				sqlexp.printStackTrace();
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				try {
					// if(pstmt.isClosed()){
					// System.out.println("pstmt close");
					// }else{
					pstmt.close();
					// }
					// if(!conn.isClosed()){
					conn.close();
					// }else{
					// System.out.println("conn close");
					// }
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	private CLOB getCLOB(String xmlData, Connection conn)
			throws SQLException {
		CLOB tempClob = null;
		try {
			// If the temporary CLOB has not yet been created, create one
			tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);
			// Open the temporary CLOB in readwrite mode, to enable writing
			tempClob.open(CLOB.MODE_READWRITE);
			// Get the output stream to write
			Writer tempClobWriter = tempClob.getCharacterOutputStream();
			// Write the data into the temporary CLOB
			tempClobWriter.write(xmlData);
			// Flush and close the stream
			tempClobWriter.flush();
			tempClobWriter.close();
			// Close the temporary CLOB
			tempClob.close();
		} catch (SQLException sqlexp) {
			tempClob.freeTemporary();
			sqlexp.printStackTrace();
		} catch (Exception exp) {
			exp.printStackTrace();
			tempClob.freeTemporary();
			
		}
		return tempClob;
	}
	@Override
	public void run() {
		System.out.println("--------MessagePushOperator-------");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		while(true) {
			try {
				SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
				List<Map<String,Object>> result = sqlMap.queryForList("getavliablepushinfo",format.format(new Date()));
				for (Map<String, Object> data : result) {
					OPAQUE opaque = (OPAQUE) data.get("CONTENT");
					XMLType xmlType = null;
					if(opaque instanceof XMLType) {
						xmlType = (XMLType)opaque;
					}else{
						xmlType = XMLType.createXML(opaque);
					}
					String content = xmlType.getStringVal();
					WXAPIServiceProcess process = new WXAPIServiceProcess();
					process.process(content);
					sqlMap.update("setpushdone",Integer.parseInt(String.valueOf(data.get("ID"))));
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(600000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		Map<String,String> para = new HashMap<String, String>();
		para.put("PUSHDAYOFWEEK","0|2|3");
		para.put("PUSHTIMEFROM","01:00");
		para.put("PUSHTIMETO","20:00");
		MessagePushOperator o = new MessagePushOperator();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(format.format(o.getNextAvailableDate(para)));
		
		System.out.println(o.getPushDayAndTime("3660475", "999-12345675", "WEIXIN"));
	}
}
