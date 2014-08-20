package com.efreight.weixin.publicservice.module;

import java.util.Date;

public class ResultModule {

	private int code;
	private String message;
	private Object result;
	private Date expireon;
	public Date getExpireon() {
		return expireon;
	}
	public void setExpireon(Date expireon) {
		this.expireon = expireon;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
}
