package com.efreight.SQL;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;

import com.efreight.commons.PropertiesUtils;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleResultSet;
import oracle.sql.CLOB;
import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

/**
 * 保存消息日志
 * 
 * @author xianan
 * 
 */
public class InsertMessageLog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InsertMessageLog l = new InsertMessageLog();
		try {
			String query = "select messageid , xml from messagelog where messageid = '988145'";
			List p = editItem(query, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 保存消息日志。
	 * 
	 * @param map
	 */
	public void insertErrorMessageLog(Map<String, String> map) {
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
			query="insert into ERRORMESSAGELOG (ERRORMESSAGEID,DATETIME,XML) values (seq_wxmessage.nextval,to_date(?,'yyyy-mm-dd hh24:mi:ss'),SYS.XMLTYPE(?))";
//			query = "insert into MESSAGELOG (messageid,datetime,sender,receiver,messagetype,content,success,wxmsgid,msgtype,issend,msgresult,xml,sendresult) values"
//					+ " (seq_wxmessage.nextval,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?,?,?,?,?,?,SYS.XMLTYPE(?),?)";
			pstmt = oracleConnection.prepareStatement(query);
			clob = getCLOB(map.get("xml"), oracleConnection);
			pstmt.setObject(1, map.get("datetime"));
			pstmt.setObject(2, clob);
			if (pstmt.executeUpdate() == 1) {
				System.out.println("Successfully inserted a errormessagelog ");
			}
		} catch (SQLException sqlexp) {
			sqlexp.printStackTrace();
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			try {
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 保存消息日志。
	 * 
	 * @param map
	 */
	public void insertXML(Map<String, String> map) {
		CLOB clob = null;
		String query;
		// Initialize statement Object
		PreparedStatement pstmt = null;
		Connection conn = null;
		Connection oracleConnection = null;
		try {
			// Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			// String str = "jdbc:oracle:thin:@192.168.0.6:1521:orcl";
			// conn = DriverManager.getConnection(str, "esinoair", "123456");
			// oracleConnection = ConnectionHandler.getConnection();

			// if (conn.isWrapperFor(OracleConnection.class)) {
			// System.out.println("没有获得连接111");
			// }
			// OracleConnection oracleConnection =
			// conn.unwrap(OracleConnection.class);
			// if(conn instanceof DelegatingConnection)
			// System.out.println("!!!!!!!!!!" + conn.getClass().getName());
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
			query = "insert into MESSAGELOG (messageid,datetime,sender,receiver,messagetype,content,success,wxmsgid,msgtype,issend,msgresult,xml,sendresult) values"
					+ " (seq_wxmessage.nextval,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?,?,?,?,?,?,SYS.XMLTYPE(?),?)";
			pstmt = oracleConnection.prepareStatement(query);
			clob = getCLOB(map.get("xml"), oracleConnection);
			pstmt.setObject(1, map.get("datetime"));
			pstmt.setObject(2, map.get("sender"));
			pstmt.setObject(3, map.get("receiver"));
			pstmt.setObject(4, map.get("messagetype"));
			pstmt.setObject(5, map.get("content"));
			pstmt.setObject(6, map.get("success"));
			pstmt.setObject(7, map.get("wxMsgId"));
			pstmt.setObject(8, map.get("msgType"));
			pstmt.setObject(9, map.get("issend"));
			pstmt.setObject(10, map.get("msgresult"));
			pstmt.setObject(11, clob);
			pstmt.setObject(12, map.get("sendresult"));
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

	/**
	 * 保存clob字段
	 * 
	 * @param xmlData
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static CLOB getCLOB(String xmlData, Connection conn)
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

	/**
	 * 读取内容
	 * 
	 * @param query
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static ArrayList editItem(String query, ArrayList params)
			throws Exception {
		Connection con = null;

		ResultSet rs = null;

		ArrayList data = new ArrayList();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			String str = "jdbc:oracle:thin:@192.168.0.6:1521:orcl";
			con = DriverManager.getConnection(str, "weixintest", "123456");

			PreparedStatement pst = con.prepareStatement(query);

			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					pst.setObject(i + 1, params.get(i));
				}
			}
			rs = pst.executeQuery();
			while (rs.next()) {
				OracleResultSet ors = (OracleResultSet) rs;
				OPAQUE op = ors.getOPAQUE(2);
				XMLType xml = XMLType.createXML(op);
				data.add(xml.getStringVal());
			}
			pst.close();
			return data;
		} catch (Exception e) {
			throw e;
		} finally {
			con.close();
		}
	}
}
