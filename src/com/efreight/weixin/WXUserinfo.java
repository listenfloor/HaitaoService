package com.efreight.weixin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 微信用户实体。
 * @author xianan
 *
 */
public class WXUserinfo extends Thread {
	public void run() {
		String xml = "<eFreightService>" + "<ServiceURL>WXAPIService</ServiceURL>"
				+ "<ServiceAction>transaction</ServiceAction>" + "<ServiceData>" + "<WXUserInfo>" + this.toString().replaceAll("&nbsp;", " ") + "</WXUserInfo>"
				+ "</ServiceData>" + "</eFreightService>";
		try {
			//post
			WXAPIServiceProcess service = new WXAPIServiceProcess();
			service.process(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String nickname;
	public String fakeid;
	public String openid;
	public String email;
	public String country;
	public String province;
	public String city;
	public String x;
	public String y;
	public String pushdayofweek;
	public String pushtimefrom;
	public String pushtimeto;
	public String language;
	public String userstatus;
	public Date lastupdate;
	public Date getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}

	public String getHeadimg() {
		return headimg;
	}

	public void setHeadimg(String headimg) {
		this.headimg = headimg;
	}

	public String headimg;


	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPushdayofweek() {
		return pushdayofweek;
	}

	public void setPushdayofweek(String pushdayofweek) {
		this.pushdayofweek = pushdayofweek;
	}

	public String getPushtimefrom() {
		return pushtimefrom;
	}

	public void setPushtimefrom(String pushtimefrom) {
		this.pushtimefrom = pushtimefrom;
	}

	public String getPushtimeto() {
		return pushtimeto;
	}

	public void setPushtimeto(String pushtimeto) {
		this.pushtimeto = pushtimeto;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getFakeid() {
		return fakeid;
	}

	public void setFakeid(String fakeid) {
		this.fakeid = fakeid;
	}

	public String getWxid() {
		return wxid;
	}

	public void setWxid(String wxid) {
		this.wxid = wxid;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String wxid;

	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(lastupdate == null)
			lastupdate = new Date();
		return "<WXUserinfo><fakeid>" + fakeid
				+ "</fakeid>" + "<nickname>" + nickname.replaceAll("&nbsp;|&lt;|&gt;|&quot;|&amp;|&copy;|&reg", "") + "</nickname>" + "<wxid>" + wxid + "</wxid>" + "<openid>" + openid + "</openid>" +
						"<country>"+country+"</country><province>"+province+"</province><city>"+city+"</city><x>"+x+"</x><y>"+y+"</y><language>"+language+"</language><headimg>"+headimg+"</headimg><lastupdate>"+format.format(lastupdate)+"</lastupdate><userstatus>"+userstatus+"</userstatus></WXUserinfo>";
	}

	public String getUserstatus() {
		return userstatus;
	}

	public void setUserstatus(String userstatus) {
		this.userstatus = userstatus;
	}
}
