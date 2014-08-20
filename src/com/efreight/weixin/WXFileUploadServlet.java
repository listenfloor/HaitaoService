package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.weixin.handler.WXMessageHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Servlet implementation class WXFileUploadServlet
 * 保存图片上传。
 */
public class WXFileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// test
//	private static final String filePath = "/Users/xianan/etc/wxUploadImage/";
	private static final String filePath = "/datadisk/tomcat/webapps/wxUploadImage/";
//	private static final String filePath = "/usr/share/tomcat6/webapps/wxUploadImage/";
	private static final String url = "/wxUploadImage/";
	private static String wxurl = "http://mp.weixin.qq.com/cgi-bin/uploadmaterial?cgi=uploadmaterial&type=0&t=iframe-uploadfile&lang=zh_CN&formId=null";
	private static Logger log = Logger.getLogger(WXFileUploadServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WXFileUploadServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response) 每次请求只接收1个文件。
	 * 首先接收文件，然后保存至本地临时文件。
	 * 之后上传至微信服务器，上传成功后，返回fileid（微信服务器上的文件id。）url本地图片路径。
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {
			// 创建磁盘工厂，利用构造器实现内存数据储存量和临时储存路径
			DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 4,
					new File(filePath + "temp/"));
			// 设置最多只允许在内存中存储的数据,单位:字节
			// factory.setSizeThreshold(4096);
			// 设置文件临时存储路径
			// factory.setRepository(new File("D:\\Temp"));
			// 产生一新的文件上传处理程式
			ServletFileUpload upload = new ServletFileUpload(factory);
			// 设置路径、文件名的字符集
			upload.setHeaderEncoding("UTF-8");
			// 设置允许用户上传文件大小,单位:字节
			upload.setSizeMax(1024 * 1024 * 100);
			// 解析请求，开始读取数据
			// Iterator<FileItem> iter = (Iterator<FileItem>)
			// upload.getItemIterator(request);
			// 得到所有的表单域，它们目前都被当作FileItem
			List<FileItem> fileItems = null;
			try {
				fileItems = upload.parseRequest(request);
			} catch (FileUploadException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String fileType = null;
			FileItem targetFile = null;
			// 依次处理请求
			Iterator<FileItem> iter = fileItems.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					// 如果item是正常的表单域
					String name = item.getFieldName();
					if (name != null && "type".equals(name)) {
						fileType = item.getString("UTF-8"); // 取上传的文件类型
					}
					// System.out.println("表单域名为:"+name+"表单域值为:"+value);
				} else {
					targetFile = item;
					// 如果item是文件上传表单域
					// 获得文件名及路径
				}
			}
			if (fileType == null || "".equals(fileType)) {
				System.out.println("the parameter: filetype is null");
				String message = getMessage("false",
						"the parameter: filetype is null", "", "");
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			}
			long l = System.currentTimeMillis();
			String fullFileName = null;
			String wxFileId = null;
			int key = 0;
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
			if (targetFile != null) {
				String fileName = targetFile.getName();
				// 生成数据库对象
				if (fileName != null) {
					// 如果文件存在则上传
					File fullFile = new File(targetFile.getName());
					fullFileName = l
							+ fullFile.getName().substring(
									fullFile.getName().lastIndexOf("."));
					File fileOnServer = new File(filePath + fullFileName);
					if (!fileOnServer.exists()) {
						fileOnServer.createNewFile();
					}
					try {
						targetFile.write(fileOnServer);
					} catch (Exception e) {
						String message = getMessage("false",
								"write wxuploadfile error", "", "");
						response.getOutputStream().write(
								message.getBytes("utf-8"));
						response.getOutputStream().flush();
						response.getOutputStream().close();
						e.printStackTrace();
						return;
					}
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss");
					Map<String, String> entryMap = new HashMap<String, String>();
					entryMap.put("fileType", fileType);
					entryMap.put("fileName", fileName);
					entryMap.put("filePath", filePath + fullFileName);
					entryMap.put("fileUrl", url + fullFileName);
					entryMap.put("createDate", format.format(new Date()));
					try {
						key = (Integer) sqlMap.insert("insertwxuploadfile",
								entryMap);// 保存
					} catch (SQLException e) {
						String message = getMessage("false",
								"insert wxuploadfile error", "", "");
						response.getOutputStream().write(
								message.getBytes("utf-8"));
						response.getOutputStream().flush();
						response.getOutputStream().close();
						e.printStackTrace();
						return;
					}
					System.out.println("文件" + fileOnServer.getName() + "上传成功");
				}
				// 数据生成完成后，开始上传微信服务器
				WXInfoDownloader loader = new WXInfoDownloader();
				try {
					loader.loginToWX();
				} catch (Exception e) {
					System.out.println("login error");
					String message = getMessage("false", "login error", "", "");
					response.getOutputStream().write(message.getBytes("utf-8"));
					response.getOutputStream().flush();
					response.getOutputStream().close();
					e.printStackTrace();
					return;
				}
				Map<String, String> cookies = loader.getCookies();
				String token = loader.getToken();
				System.out.println("token:" + token);
				System.out.println("cookies:" + cookies);
				HttpPostUploadUtil httppostutil = new HttpPostUploadUtil(token,
						cookies);
				// 上传地址一样，微信服务器是根据文件格式区分。
				wxurl += "&token=" + token;
				Map<String, String> textMap = new HashMap<String, String>();
				textMap.put("formId", "");
				Map<String, String> fileMap = new HashMap<String, String>();
				fileMap.put("uploadfile", filePath + fullFileName);
				String ret = httppostutil.formUpload(wxurl, textMap, fileMap);
				// 解析是否成功。
				Pattern regex = Pattern.compile("formId, '(.*?)'");
				Matcher matcher = regex.matcher(ret);
				if (matcher.find()) {// 上传成功
					wxFileId = matcher.group(1);
				} else {// 上传失败
					System.out.println("file upload to weixin error.");
					String message = getMessage("false",
							"file upload to weixin error.", "", "");
					response.getOutputStream().write(message.getBytes("utf-8"));
					response.getOutputStream().flush();
					response.getOutputStream().close();
					return;
				}
				// 开始更新数据库
				Map<String, String> entryMap = new HashMap<String, String>();
				entryMap.put("sys_code", key + "");
				entryMap.put("wxFileId", wxFileId);
				try {
					sqlMap.update("updatewxuploadfile", entryMap);
				} catch (SQLException e) {
					System.out.println("update wxfileid error");
					String message = getMessage("false",
							"update wxfileid error", "", "");
					response.getOutputStream().write(message.getBytes("utf-8"));
					response.getOutputStream().flush();
					response.getOutputStream().close();
					e.printStackTrace();
					return;
				}
				// 组织返回数据
				String message = getMessage("true", "success", wxFileId, url
						+ fullFileName);
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			} else {
				System.out.println("file upload is error. the file is null");
				String message = getMessage("false", url + fullFileName, "", "");
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			}
		}
	}

	/**
	 * 返回提示
	 * 
	 * @param message
	 * @return
	 */
	private String getMessage(String status, String message, String fileId,
			String url) {
		StringBuffer sb = new StringBuffer("{\"status\":\"" + status
				+ "\",\"description\":\"" + message + "\" , \"fileid\":\""
				+ fileId + "\" ,\"url\":\"" + url + "\"}");
		return sb.toString();
	}

}
