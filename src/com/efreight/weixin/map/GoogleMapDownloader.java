package com.efreight.weixin.map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleMapDownloader implements Runnable {
	private String lat = "";
	private String lng = "";
	private String width = "";
	private String height = "";
	private String ac3code = "";
	
	public GoogleMapDownloader (String lat,String lng,String width,String height,String ac3code) {
		this.lat = lat;
		this.lng = lng;
		this.width = width;
		this.height = height;
		this.ac3code = ac3code;
	}
	@Override
	public void run() {
		try{
		HttpURLConnection request = (HttpURLConnection) new URL(
				"http://maps.googleapis.com/maps/api/staticmap?center=" + this.lat + "," + this.lng
						+ "&zoom=12&size="+width+"x"+height+"&sensor=false&key=AIzaSyBe7tQdhil-XNBDX2aVpCVuJp93GqwKV1I&markers=color:blue%7Clabel:A%7C"+lat+","+lng).openConnection();
		request.setRequestMethod("GET");
		request.connect();
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader((InputStream) request.getInputStream()));
		InputStream in = request.getInputStream();
		FileOutputStream fos = new FileOutputStream(new File("/datadisk/apache/htdocs/airportmap", ac3code + ".png"));
		int i = -1;
		while ((i = in.read()) != -1) {
			fos.write(i);
		}    
		fos.flush();
		fos.close();
		in.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
