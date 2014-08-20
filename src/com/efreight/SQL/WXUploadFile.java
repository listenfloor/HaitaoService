package com.efreight.SQL;

public class WXUploadFile {

	/**
	 * 主键，自增
	 */
	private int sys_code;
	
	/**
	 * 文件名
	 */
	private String fileName;
	
	/**
	 * 文件类型 2:图片 3:音频 4:视频
	 */
	private int fileType;
	
	/**
	 * 创建时间
	 */
	private String createDate;
	
	/**
	 * 本地路径
	 */
	private String filePath;
	
	/**
	 * 微信文件id
	 */
	private String wxFileId;
	
	/**
	 * 访问路径
	 */
	private String fileUrl;

	public int getSys_code() {
		return sys_code;
	}

	public void setSys_code(int sys_code) {
		this.sys_code = sys_code;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getWxFileId() {
		return wxFileId;
	}

	public void setWxFileId(String wxFileId) {
		this.wxFileId = wxFileId;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	
	
}
