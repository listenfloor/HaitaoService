package com.efreight.SQL;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;

import com.efreight.commons.PropertiesUtils;

public class ConnectionHandler {

	public static Connection getConnection(){
		Connection conn=null;
		Connection oracleConnection = null;
//		if (conn.isWrapperFor(OracleConnection.class)) {
//			System.out.println("没有获得连接111");
//		}
//		OracleConnection oracleConnection = conn.unwrap(OracleConnection.class);
//		if(conn instanceof DelegatingConnection)
//		System.out.println("!!!!!!!!!!" + conn.getClass().getName());
			try {
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup(PropertiesUtils.readProductValue("", "jndipath"));
				conn = ds.getConnection();
				DelegatingConnection delegatingConn = (DelegatingConnection)conn;
				oracleConnection = delegatingConn.getInnermostDelegate();
				if(oracleConnection == null)
					oracleConnection = delegatingConn.getMetaData().getConnection();
				oracleConnection = ((DelegatingConnection)oracleConnection).getDelegate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return oracleConnection;
	}
	
	public static void closeConnetion(Connection conn){
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
