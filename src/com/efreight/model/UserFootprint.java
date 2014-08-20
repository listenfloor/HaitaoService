package com.efreight.model;

import java.util.Date;

public class UserFootprint {

	private String id;
	
	/**
	 * 纪录url
	 */
	private String url;
	
	/**
	 * 服务：通过url来判断用户使用的是那种服务 tact/wabtrace/help/index/
	 * 
	 */
	private String service;
	
	/**
	 * 访问时间
	 */
	private Date createTime;
	
	/**
	 * 是否是转发
	 */
	private String isTransmit;
	
	/**
	 * 用户id：其中可能有fakeid，native的id，其他id等。
	 */
	private String userid; 
	
	/**
	 * 来源，字段为：weixin/ios/android……
	 */
	private String resource;
}
