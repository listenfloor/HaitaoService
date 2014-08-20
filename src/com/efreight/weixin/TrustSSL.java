package com.efreight.weixin;

import java.io.*;
import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class TrustSSL {
	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	public static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static void main(String[] args) throws Exception {
		InputStream in = null;
		OutputStream out = null;
		byte[] buffer = new byte[4096];
		String str_return = "";
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
					new java.security.SecureRandom());
			URL console = new URL("https://mp.weixin.qq.com/cgi-bin/loginpage?t=wxm2-login&lang=zh_CN");
			HttpsURLConnection conn = (HttpsURLConnection) console
					.openConnection();
			conn.setSSLSocketFactory(sc.getSocketFactory());
			conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
			conn.connect();
			InputStream is = conn.getInputStream();
			DataInputStream indata = new DataInputStream(is);
			String ret = "";

			while (ret != null) {
				ret = indata.readLine();
				if (ret != null && !ret.trim().equals("")) {
					str_return = str_return
							+ new String(ret.getBytes("ISO-8859-1"), "utf-8");
				}
			}
			conn.disconnect();
		} catch (ConnectException e) {
			System.out.println("ConnectException");
			System.out.println(e);
			throw e;

		} catch (IOException e) {
			System.out.println("IOException");
			System.out.println(e);
			throw e;

		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
			try {
				out.close();
			} catch (Exception e) {
			}
		}
		System.out.println(str_return);
	}
}