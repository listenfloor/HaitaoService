package com.efreight.commons;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 公用方法
 * @author xianan
 *
 */
public class CommUtil {
	/**
	 * md5加密
	 * @param str 要加密字符串
	 * @return 加密过后字符串
	 */
	public static String getMD5Str(String str) { 
        MessageDigest messageDigest = null; 
        try { 
            messageDigest = MessageDigest.getInstance("MD5"); 
            messageDigest.reset(); 
            messageDigest.update(str.getBytes("UTF-8")); 
        } catch (NoSuchAlgorithmException e) { 
            System.out.println("NoSuchAlgorithmException caught!"); 
            System.exit(-1); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        } 
        byte[] byteArray = messageDigest.digest(); 
        StringBuffer md5StrBuff = new StringBuffer(); 
        for (int i = 0; i < byteArray.length; i++) {             
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) 
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i])); 
            else 
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i])); 
        } 
        return md5StrBuff.toString(); 
    }

	/**
	 * base64解码
	 * @param s
	 * @return String
	 */
	public static String getFromBASE64(String s) {
		if (s == null)
			return null;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(s);
			return new String(b,"UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * base64编码
	 * @param s
	 * @return String
	 */
	public static String getBASE64(String s) throws Exception{ 
		if (s == null) return null; 
		return (new sun.misc.BASE64Encoder()).encode( s.getBytes("UTF-8") ); 
	}
	
	
	public static boolean isNullOrEmpty(String s) throws Exception {
		if(s == null || "".equals(s))
			return true;
		return false;
	}
	
	public static void main(String[] args) {
		String stardard_data ="您订a阅的运单您订阅的运单您订阅的d运单您订阅的运单您订d阅的运单您订阅d的运单您订阅的运单您订d阅的运单您订阅的d运单您订阅的运单您订阅的运d单您订阅的d运单您订阅的运单d您订阅的运单您订d阅的运单您订阅d的运单您订d阅的运单您d订阅的运单您订阅的运单您订阅的运单";
		String messagedata = "{\"awbcode\":\"999-12345678\"";
//		您订阅的运单{{awbcode.DATA}}有新的轨迹：{{date.DATA}}{{summary.DATA}}{{cargocode.DATA}}{{datasource.DATA}}{{warning.DATA}}{{description.DATA}}{{comment1.DATA}}{{comment2.DATA}}
//		回复"详细"或直接回复运单号查看运单轨迹明细。
		int length = stardard_data.getBytes().length;
//		int paraLength = 1;
		if(length > 20) {
//			paraLength = length%20==0?length/20:length/20 +1;
//			paraLength = paraLength >8?8:paraLength;
			int currentPosition = 0;
			int strLength = 0;
			char[] data = stardard_data.toCharArray();
			boolean full = false;
			List<Character> tempData = new ArrayList<Character>();
			for (int i = 0; i < data.length; i++) {
				System.out.println(strLength);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(strLength < 20) {
					if((int)data[i]>256 && strLength < 19) {
						strLength += 2;
						tempData.add(data[i]);
					}
					else if((int)data[i] <=256){
						strLength ++;
						tempData.add(data[i]);
					}
					else 
						full = true;
				}else
					full = true;
				
				
				if(full || i == data.length-1) {
					full = false;
					if(i != data.length-1)
					i--;
					String tmp = "";
					for (int j = 0; j < tempData.size(); j++) {
						tmp += tempData.get(j);
					}
					tempData.clear();
					strLength = 0;
					System.out.println(tmp);
					switch (currentPosition) {
					case 0:
						messagedata += ",\"date\":\""+tmp+"\"";
						break;
					case 1:
						messagedata += ",\"summary\":\""+tmp+"\"";
						break;
					case 2:
						messagedata += ",\"cargocode\":\""+tmp+"\"";
						break;
					case 3:
						messagedata += ",\"datasource\":\""+tmp+"\"";
						break;
					case 4:
						messagedata += ",\"warning\":\""+tmp+"\"";
						break;
					case 5:
						messagedata += ",\"description\":\""+tmp+"\"";
						break;
					case 6:
						messagedata += ",\"comment1\":\""+tmp+"\"";
						break;
					case 7:
						messagedata += ",\"comment2\":\""+tmp+"\"";
						break;
	
					}
					currentPosition++;
				}
			}
		}
		messagedata +="}";
		System.out.println(messagedata);
	}
}
