package com.efreight.weixin;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import oracle.xdb.XMLType;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Servlet implementation class RepeatMessage
 */
public class RepeatMessage extends HttpServlet {
	private static final long serialVersionUID = 1L;
     private Logger log = Logger.getLogger(RepeatMessage.class);
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RepeatMessage() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String msgId = request.getParameter("msgid");
		log.info("RepeatMessage start msgid:"+msgId);
		try{
			msgId = msgId.trim();
		}catch(Exception e){
		}
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String respStr = "";
		if(msgId==null||"".equals(msgId)){
			respStr 	="发送失败";
			response.getOutputStream().write(respStr.getBytes("utf-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return;
		}
		
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		Map<String,String> requestMap = new HashMap<String, String>();
		requestMap.put("messageid", msgId);
		try {
			Map<String,Object> objs = (Map<String,Object>)sqlMap.queryForObject("loadmessagelogbyid",requestMap);
			if(objs==null||objs.size()<=0){
				respStr 	="未能找到id为"+msgId+"的消息";
				response.getOutputStream().write(respStr.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			}
			int issend = -1;
			try{
				issend = ((BigDecimal)objs.get("ISSEND")).intValue();
			}catch(Exception e){
				log.info("查询issend报错:"+e.getMessage());
			}
			if(issend==-1){
				respStr 	="重发消息出错";
				response.getOutputStream().write(respStr.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
			if(objs.containsKey("XML")){
				XMLType xmlType = (XMLType)objs.get("XML");
				String xml = xmlType.getStringVal();
				//是否需要判断发送/接收
				if(xml!=null&&!"".equals(xml)){
					if(issend==0){//判断是否是模拟用户重新发送
						HttpHandler.postXml(xml);
					}else{
						WXAPIServiceProcess process = new WXAPIServiceProcess();
						String result = process.process(xml);
					}
					respStr = "重发消息完成";
					response.getOutputStream().flush();
					response.getOutputStream().close();
					return;
				}
			}
		} catch (SQLException e) {
			respStr 	="查询出错";
			response.getOutputStream().write(respStr.getBytes("utf-8"));
			response.getOutputStream().flush();
			response.getOutputStream().close();
			e.printStackTrace();
			return;
		}
	}

}
