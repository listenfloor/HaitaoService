package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.efreight.commons.HttpHandler;
import com.efreight.commons.CommUtil;
import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.process.WXProcessHandler;

public class Test {

	private final static String USERNAME = "efreight";
	private final static String PASSWORD = "pass@word";
	private final static String LOGINURL = "https://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN";
	private final static String INDEXURL = "https://mp.weixin.qq.com/cgi-bin/indexpage?t=wxm-index&lang=zh_CN";
	private static Map<String, String> groupInfo;
	public static Map<String, WXUserinfo> userWithFakeId;
	public static Map<String, WXUserinfo> userWithWXId;
	private static List<WXUserinfo> updateUser;
	private static String token = "";

	private Map<String, String> cookies = new HashMap<String, String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String xml = "<Service><ServiceURL>QueryTACT</ServiceURL><ServiceAction>queryTactByDestination_ap</ServiceAction><ServiceData><destination>FRA</destination></ServiceData><PageSize>100</PageSize><CurrentPage>1</CurrentPage></Service>";
//		String responseStr = HttpHandler.postHttpRequest("http://192.168.0.218:8001/HttpEngine", xml);
//		System.out.println(responseStr);
		String newxml =
				"<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>SendTracePushMessageWithTemplateAPI</ServiceAction><ServiceData><wxfakeid>1118130022</wxfakeid><messageid>10000125</messageid><messageitem><title>运单 : 112-04220996轨迹</title><digest>5分钟内回复“Y”即可订阅最后查询的运单。</digest><fileid>10000051</fileid><content>PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbmgIHlkI3n"+
						"p7A6ICDliLDovr48QlIvPuiIquermeWQjeensDogIOW+t+mHjDxCUi8+6Iiq54+t5Y+3OiAgTVU1"+
						"NjM8QlIvPuaXpeacnzogIDIwMTMtMTItMjM8QlIvPuaXtumXtDogIDAwOjMzPEJSLz7nirbmgIHm"+
						"j4/ov7A6ICDotKfnianlkozmlofku7blt7LnlLHmjIflrproiKrnj63ov5Dovr7lubbooqvmjqXm"+
						"lLY8QlIvPuS7tuaVsDogIDQ8QlIvPumHjemHjzogIDIwOTU8L3NwYW4+PC9wPjxwPjxociBzdHls"+
						"ZT0iY29sb3I6I2Q4ZDhkODsgd2lkdGg6OTglO2JvcmRlci1zdHlsZTogZGFzaGVkOyIvPjwvcD48"+
						"cD48c3BhbiBzdHlsZT0iY29sb3I6IzdmN2Y3Zjtmb250LXNpemU6MTJweDsiPueKtuaAgeWQjeen"+
						"sDogIOi0p+eJqeWIsOi+vjxCUi8+6Iiq56uZ5ZCN56ewOiAg5b636YeMPEJSLz7oiKrnj63lj7c6"+
						"ICBNVTU2MzxCUi8+5pel5pyfOiAgMjAxMzEyMjI8QlIvPuaXtumXtDogIDIyOjA0PEJSLz7nirbm"+
						"gIHmj4/ov7A6ICDotKfnianlt7LnlLHmjIflrproiKrnj63ov5Dovr7lubbooqvmjqXmlLY8QlIv"+
						"PuS7tuaVsDogIDQ8QlIvPumHjemHjzogIDIwOTU8L3NwYW4+PC9wPjxwPjxociBzdHlsZT0iY29s"+
						"b3I6I2Q4ZDhkODsgd2lkdGg6OTglO2JvcmRlci1zdHlsZTogZGFzaGVkOyIvPjwvcD48cD48c3Bh"+
						"biBzdHlsZT0iY29sb3I6IzdmN2Y3Zjtmb250LXNpemU6MTJweDsiPueKtuaAgeWQjeensDogIOWH"+
						"uuWPkTxCUi8+6Iiq56uZ5ZCN56ewOiAg5LiK5rW35rWm5LicPEJSLz7oiKrnj63lj7c6ICBNVTU2"+
						"MzxCUi8+5pel5pyfOiAgMjAxMy0xMi0yMjxCUi8+5pe26Ze0OiAgMTM6Mzk8QlIvPueKtuaAgeaP"+
						"j+i/sDogIOi0p+eJqeW3sueUseaMh+WumuiIquePreS7juS4iua1t+a1puS4nOi/kOWHuiwg5YmN"+
						"5b6A5b636YeMLOiIquePreWunumZhei1t+mjnuaXtumXtDEzMzksIOiuoeWIkuWIsOi+vuaXtumX"+
						"tCAxOTQwPEJSLz7ku7bmlbA6ICA0PEJSLz7ph43ph486ICAyMDk1PC9zcGFuPjwvcD48cD48aHIg"+
						"c3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hlZDsiLz48"+
						"L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbmgIHl"+
						"kI3np7A6ICDotKfnianphY3ovb08QlIvPuiIquermeWQjeensDogIOS4iua1t+a1puS4nDxCUi8+"+
						"6Iiq54+t5Y+3OiAgTVU1NjM8QlIvPuaXpeacnzogIDIwMTMtMTItMjI8QlIvPuaXtumXtDogIDEz"+
						"OjQwPEJSLz7nirbmgIHmj4/ov7A6ICDotKfnianlt7LooqvphY3kuIrmjIflrproiKrnj60s6K6h"+
						"5YiS5LuOIOS4iua1t+a1puS4nCDov5DlvoAg5b636YeMIOiIquePreiuoeWIkui1t+mjnuaXtumX"+
						"tCAxMzQwLOiuoeWIkuWIsOi+vuaXtumXtCAxOTQwPEJSLz7ku7bmlbA6ICA0PEJSLz7ph43ph486"+
						"ICBTTEFDIFBpZWNlOjQ8L3NwYW4+PC9wPjxwPjxociBzdHlsZT0iY29sb3I6I2Q4ZDhkODsgd2lk"+
						"dGg6OTglO2JvcmRlci1zdHlsZTogZGFzaGVkOyIvPjwvcD48cD48c3BhbiBzdHlsZT0iY29sb3I6"+
						"IzdmN2Y3Zjtmb250LXNpemU6MTJweDsiPueKtuaAgeWQjeensDogIOi0p+eJqemihOmFjTxCUi8+"+
						"6Iiq56uZ5ZCN56ewOiAg5LiK5rW35rWm5LicPEJSLz7oiKrnj63lj7c6ICBNVTU2MzxCUi8+5pel"+
						"5pyfOiAgMjAxMy0xMi0yMjxCUi8+5pe26Ze0OiAgMTM6NDA8QlIvPueKtuaAgeaPj+i/sDogIOi0"+
						"p+eJqeW3sumihOmFjeS4iuaMh+WumuiIquePrSzorqHliJLku44g5LiK5rW35rWm5LicIOi/kOW+"+
						"gCDlvrfph4wg6Iiq54+t6K6h5YiS6LW36aOe5pe26Ze0IDEzNDAsIOiuoeWIkuWIsOi+vuaXtumX"+
						"tCAxOTQwPEJSLz7ku7bmlbA6ICA0PEJSLz7ph43ph486ICAyMDk1PC9zcGFuPjwvcD48cD48aHIg"+
						"c3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hlZDsiLz48"+
						"L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbmgIHl"+
						"kI3np7A6ICDov5vmuK/liLDovr7kv67mlLk8QlIvPuiIquermeWQjeensDogIOWMl+S6rDxCUi8+"+
						"6Iiq54+t5Y+3OiAgPEJSLz7ml6XmnJ86ICAyMDEzLTEyLTIyPEJSLz7ml7bpl7Q6ICAxMDo1MjxC"+
						"Ui8+54q25oCB5o+P6L+wOiAg6LSn54mp5ZKM5paH5Lu25bey55Sx5oyH5a6a6Iiq54+t6L+Q6L6+"+
						"5bm26KKr5o6l5pS2PEJSLz7ku7bmlbA6ICA0PEJSLz7ph43ph486ICAyMDk1PC9zcGFuPjwvcD48"+
						"cD48aHIgc3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hl"+
						"ZDsiLz48L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7n"+
						"irbmgIHlkI3np7A6ICDlh7rlj5E8QlIvPuiIquermeWQjeensDogIOS4iua1t+iZueahpTxCUi8+"+
						"6Iiq54+t5Y+3OiAgQ0s0MDA3PEJSLz7ml6XmnJ86ICAyMDEzLTEyLTIxPEJSLz7ml7bpl7Q6ICAx"+
						"MDowMDxCUi8+54q25oCB5o+P6L+wOiAg6LSn54mp5bey55Sx5oyH5a6a6Iiq54+t5LuO5LiK5rW3"+
						"6Jm55qGl6L+Q5Ye6LCDliY3lvoDkuIrmtbfmtabkuJws6Iiq54+t5a6e6ZmF6LW36aOe5pe26Ze0"+
						"MTAwMCwg6K6h5YiS5Yiw6L6+5pe26Ze0IDAwMDA8QlIvPuS7tuaVsDogIDQ8QlIvPumHjemHjzog"+
						"IDIwOTU8L3NwYW4+PC9wPjxwPjxociBzdHlsZT0iY29sb3I6I2Q4ZDhkODsgd2lkdGg6OTglO2Jv"+
						"cmRlci1zdHlsZTogZGFzaGVkOyIvPjwvcD48cD48c3BhbiBzdHlsZT0iY29sb3I6IzdmN2Y3Zjtm"+
						"b250LXNpemU6MTJweDsiPueKtuaAgeWQjeensDogIOi0p+eJqemihOmFjTxCUi8+6Iiq56uZ5ZCN"+
						"56ewOiAg5LiK5rW36Jm55qGlPEJSLz7oiKrnj63lj7c6ICBDSzQwMDc8QlIvPuaXpeacnzogIDIw"+
						"MTMtMTItMjE8QlIvPuaXtumXtDogIDAwOjAwPEJSLz7nirbmgIHmj4/ov7A6ICDotKfnianlt7Lp"+
						"ooTphY3kuIrmjIflrproiKrnj60s6K6h5YiS5LuOIOS4iua1t+iZueahpSDov5DlvoAg5LiK5rW3"+
						"5rWm5LicIOiIquePreiuoeWIkui1t+mjnuaXtumXtCAwMDAwLCDorqHliJLliLDovr7ml7bpl7Qg"+
						"MDAwMDxCUi8+5Lu25pWwOiAgNDxCUi8+6YeN6YePOiAgMjA5NTwvc3Bhbj48L3A+PHA+PGhyIHN0"+
						"eWxlPSJjb2xvcjojZDhkOGQ4OyB3aWR0aDo5OCU7Ym9yZGVyLXN0eWxlOiBkYXNoZWQ7Ii8+PC9w"+
						"PjxwPjxzcGFuIHN0eWxlPSJjb2xvcjojN2Y3ZjdmO2ZvbnQtc2l6ZToxMnB4OyI+54q25oCB5ZCN"+
						"56ewOiAg5Ye65Y+RPEJSLz7oiKrnq5nlkI3np7A6ICDljJfkuqw8QlIvPuiIquePreWPtzogIE1V"+
						"NTE0MDxCUi8+5pel5pyfOiAgMjAxMy0xMi0yMDxCUi8+5pe26Ze0OiAgMTQ6NTg8QlIvPueKtuaA"+
						"geaPj+i/sDogIOi0p+eJqeW3sueUseaMh+WumuiIquePreS7juWMl+S6rOi/kOWHuiwg5YmN5b6A"+
						"5LiK5rW36Jm55qGlLOiIquePreWunumZhei1t+mjnuaXtumXtDE0NTgsIOiuoeWIkuWIsOi+vuaX"+
						"tumXtCAxNjQwPEJSLz7ku7bmlbA6ICA0PEJSLz7ph43ph486ICAyMDk1PC9zcGFuPjwvcD48cD48"+
						"aHIgc3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hlZDsi"+
						"Lz48L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbm"+
						"gIHlkI3np7A6ICDmlofku7bliLDovr48QlIvPuiIquermeWQjeensDogIOS4iua1t+iZueahpTxC"+
						"Ui8+6Iiq54+t5Y+3OiAgTVU1MTQwPEJSLz7ml6XmnJ86ICAyMDEzLTEyLTIwPEJSLz7ml7bpl7Q6"+
						"ICAxNzo0NDxCUi8+54q25oCB5o+P6L+wOiAg5paH5Lu25bey55Sx5oyH5a6a6Iiq54+t6L+Q6L6+"+
						"5bm26KKr5o6l5pS2PEJSLz7ku7bmlbA6ICA0PEJSLz7ph43ph486ICAyMDk1PC9zcGFuPjwvcD48"+
						"cD48aHIgc3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hl"+
						"ZDsiLz48L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7n"+
						"irbmgIHlkI3np7A6ICDlvoXovazov5A8QlIvPuiIquermeWQjeensDogIOS4iua1t+iZueahpTxC"+
						"Ui8+6Iiq54+t5Y+3OiAgPEJSLz7ml6XmnJ86ICAyMDEzLTEyLTIwPEJSLz7ml7bpl7Q6ICAxNzo0"+
						"NDxCUi8+54q25oCB5o+P6L+wOiAg6LSn54mp5b6F6L2s6L+QPEJSLz7ku7bmlbA6ICA0PEJSLz7p"+
						"h43ph486ICAyMDk1PC9zcGFuPjwvcD48cD48aHIgc3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdpZHRo"+
						"Ojk4JTtib3JkZXItc3R5bGU6IGRhc2hlZDsiLz48L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3"+
						"ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbmgIHlkI3np7A6ICDotKfnianliLDovr48QlIvPuiI"+
						"quermeWQjeensDogIOS4iua1t+iZueahpTxCUi8+6Iiq54+t5Y+3OiAgTVU1MTQwPEJSLz7ml6Xm"+
						"nJ86ICAyMDEzLTEyLTIwPEJSLz7ml7bpl7Q6ICAxNzo0NDxCUi8+54q25oCB5o+P6L+wOiAg6LSn"+
						"54mp5bey55Sx5oyH5a6a6Iiq54+t6L+Q6L6+5bm26KKr5o6l5pS2PEJSLz7ku7bmlbA6ICA0PEJS"+
						"Lz7ph43ph486ICAyMDk1PC9zcGFuPjwvcD48cD48aHIgc3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdp"+
						"ZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hlZDsiLz48L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9y"+
						"OiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbmgIHlkI3np7A6ICDliLDovr48QlIvPuiIquer"+
						"meWQjeensDogIOS4iua1t+iZueahpTxCUi8+6Iiq54+t5Y+3OiAgTVU1MTQwPEJSLz7ml6XmnJ86"+
						"ICAyMDEzLTEyLTIwPEJSLz7ml7bpl7Q6ICAxNzo0NDxCUi8+54q25oCB5o+P6L+wOiAg6LSn54mp"+
						"5ZKM5paH5Lu25bey55Sx5oyH5a6a6Iiq54+t6L+Q6L6+5bm26KKr5o6l5pS2PEJSLz7ku7bmlbA6"+
						"ICA0PEJSLz7ph43ph486ICAyMDk1PC9zcGFuPjwvcD48cD48aHIgc3R5bGU9ImNvbG9yOiNkOGQ4"+
						"ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hlZDsiLz48L3A+PHA+PHNwYW4gc3R5bGU9"+
						"ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbmgIHlkI3np7A6ICDotKfnianphY3o"+
						"vb08QlIvPuiIquermeWQjeensDogIOWMl+S6rDxCUi8+6Iiq54+t5Y+3OiAgTVU1MTQwPEJSLz7m"+
						"l6XmnJ86ICAyMDEzLTEyLTIwPEJSLz7ml7bpl7Q6ICAxNDozMDxCUi8+54q25oCB5o+P6L+wOiAg"+
						"6LSn54mp5bey6KKr6YWN5LiK5oyH5a6a6Iiq54+tLOiuoeWIkuS7jiDljJfkuqwg6L+Q5b6AIOS4"+
						"iua1t+iZueahpSDoiKrnj63orqHliJLotbfpo57ml7bpl7QgMTQzMCzorqHliJLliLDovr7ml7bp"+
						"l7QgMTY0MDxCUi8+5Lu25pWwOiAgNDxCUi8+6YeN6YePOiAgU0xBQyBQaWVjZTo0PC9zcGFuPjwv"+
						"cD48cD48aHIgc3R5bGU9ImNvbG9yOiNkOGQ4ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRh"+
						"c2hlZDsiLz48L3A+PHA+PHNwYW4gc3R5bGU9ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7"+
						"Ij7nirbmgIHlkI3np7A6ICDotKfnianpooTphY08QlIvPuiIquermeWQjeensDogIOWMl+S6rDxC"+
						"Ui8+6Iiq54+t5Y+3OiAgTVU1MTQwPEJSLz7ml6XmnJ86ICAyMDEzLTEyLTIwPEJSLz7ml7bpl7Q6"+
						"ICAxNDozMDxCUi8+54q25oCB5o+P6L+wOiAg6LSn54mp5bey6aKE6YWN5LiK5oyH5a6a6Iiq54+t"+
						"LOiuoeWIkuS7jiDljJfkuqwg6L+Q5b6AIOS4iua1t+iZueahpSDoiKrnj63orqHliJLotbfpo57m"+
						"l7bpl7QgMTQzMCwg6K6h5YiS5Yiw6L6+5pe26Ze0IDE2NDA8QlIvPuS7tuaVsDogIDQ8QlIvPumH"+
						"jemHjzogIDIwOTU8L3NwYW4+PC9wPjxwPjxociBzdHlsZT0iY29sb3I6I2Q4ZDhkODsgd2lkdGg6"+
						"OTglO2JvcmRlci1zdHlsZTogZGFzaGVkOyIvPjwvcD48cD48c3BhbiBzdHlsZT0iY29sb3I6Izdm"+
						"N2Y3Zjtmb250LXNpemU6MTJweDsiPueKtuaAgeWQjeensDogIOW3suaKpeWFszxCUi8+6Iiq56uZ"+
						"5ZCN56ewOiAg5YyX5LqsPEJSLz7oiKrnj63lj7c6ICA8QlIvPuaXpeacnzogIDIwMTMtMTItMTU8"+
						"QlIvPuaXtumXtDogIDE4OjQ2PEJSLz7nirbmgIHmj4/ov7A6ICDotKfniankv6Hmga/lt7LkvKDo"+
						"vpPnu5nmtbflhbM8QlIvPuS7tuaVsDogIDQ8QlIvPumHjemHjzogIDIwOTU8L3NwYW4+PC9wPjxw"+
						"PjxociBzdHlsZT0iY29sb3I6I2Q4ZDhkODsgd2lkdGg6OTglO2JvcmRlci1zdHlsZTogZGFzaGVk"+
						"OyIvPjwvcD48cD48c3BhbiBzdHlsZT0iY29sb3I6IzdmN2Y3Zjtmb250LXNpemU6MTJweDsiPueK"+
						"tuaAgeWQjeensDogIOaUtui0pzxCUi8+6Iiq56uZ5ZCN56ewOiAg5YyX5LqsPEJSLz7oiKrnj63l"+
						"j7c6ICA8QlIvPuaXpeacnzogIDIwMTMtMTItMTU8QlIvPuaXtumXtDogIDE4OjQ2PEJSLz7nirbm"+
						"gIHmj4/ov7A6ICDku47lj5HotKfkurrmiJblhbbku6PnkIYgR1NIIOaJi+S4reaUtuWIsOWHuua4"+
						"r+i0p+eJqeWSjOaWh+S7tjxCUi8+5Lu25pWwOiAgNDxCUi8+6YeN6YePOiAgMjA5NTwvc3Bhbj48"+
						"L3A+PHA+PGhyIHN0eWxlPSJjb2xvcjojZDhkOGQ4OyB3aWR0aDo5OCU7Ym9yZGVyLXN0eWxlOiBk"+
						"YXNoZWQ7Ii8+PC9wPjxwPjxzcGFuIHN0eWxlPSJjb2xvcjojN2Y3ZjdmO2ZvbnQtc2l6ZToxMnB4"+
						"OyI+54q25oCB5ZCN56ewOiAg6K6i6Iix56Gu6K6kPEJSLz7oiKrnq5nlkI3np7A6ICDkuIrmtbfm"+
						"tabkuJw8QlIvPuiIquePreWPtzogIE1VNTY3PEJSLz7ml6XmnJ86ICAyMDEzLTEyLTE2PEJSLz7m"+
						"l7bpl7Q6ICAxMDowNTxCUi8+54q25oCB5o+P6L+wOiAg6LSn54mp5bey6K6i5aal5oyH5a6a6Iiq"+
						"54+t55qE6Iix5L2NLOiuoeWIkuS7jiDkuIrmtbfmtabkuJwg6L+Q5b6AIOaWsOWKoOWdoTxCUi8+"+
						"5Lu25pWwOiAgNDxCUi8+6YeN6YePOiAgMjA4MDwvc3Bhbj48L3A+PHA+PGhyIHN0eWxlPSJjb2xv"+
						"cjojZDhkOGQ4OyB3aWR0aDo5OCU7Ym9yZGVyLXN0eWxlOiBkYXNoZWQ7Ii8+PC9wPjxwPjxzcGFu"+
						"IHN0eWxlPSJjb2xvcjojN2Y3ZjdmO2ZvbnQtc2l6ZToxMnB4OyI+54q25oCB5ZCN56ewOiAg6K6i"+
						"6Iix56Gu6K6kPEJSLz7oiKrnq5nlkI3np7A6ICDljJfkuqw8QlIvPuiIquePreWPtzogIE1VNTYz"+
						"PEJSLz7ml6XmnJ86ICAyMDEzLTEyLTE1PEJSLz7ml7bpl7Q6ICAwNzoyNTxCUi8+54q25oCB5o+P"+
						"6L+wOiAg6LSn54mp5bey6K6i5aal5oyH5a6a6Iiq54+t55qE6Iix5L2NLOiuoeWIkuS7jiDljJfk"+
						"uqwg6L+Q5b6AIOS4iua1t+a1puS4nCDoiKrnj63orqHliJLotbfpo57ml7bpl7QgMDcyNSwg6K6h"+
						"5YiS5Yiw6L6+5pe26Ze0IDA5NTU8QlIvPuS7tuaVsDogIDQ8QlIvPumHjemHjzogIDIwODA8L3Nw"+
						"YW4+PC9wPjxwPjxociBzdHlsZT0iY29sb3I6I2Q4ZDhkODsgd2lkdGg6OTglO2JvcmRlci1zdHls"+
						"ZTogZGFzaGVkOyIvPjwvcD48cD48c3BhbiBzdHlsZT0iY29sb3I6IzdmN2Y3Zjtmb250LXNpemU6"+
						"MTJweDsiPueKtuaAgeWQjeensDogIOiuouiIseehruiupDxCUi8+6Iiq56uZ5ZCN56ewOiAg5YyX"+
						"5LqsPEJSLz7oiKrnj63lj7c6ICBNVTU2MzxCUi8+5pel5pyfOiAgMjAxMy0xMi0xMzxCUi8+5pe2"+
						"6Ze0OiAgMDc6MjU8QlIvPueKtuaAgeaPj+i/sDogIOi0p+eJqeW3suiuouWmpeaMh+WumuiIqueP"+
						"reeahOiIseS9jSzorqHliJLku44g5YyX5LqsIOi/kOW+gCDkuIrmtbfmtabkuJwg6Iiq54+t6K6h"+
						"5YiS6LW36aOe5pe26Ze0IDA3MjUsIOiuoeWIkuWIsOi+vuaXtumXtCAwOTU1PEJSLz7ku7bmlbA6"+
						"ICA0PEJSLz7ph43ph486ICAyMTAwPC9zcGFuPjwvcD48cD48aHIgc3R5bGU9ImNvbG9yOiNkOGQ4"+
						"ZDg7IHdpZHRoOjk4JTtib3JkZXItc3R5bGU6IGRhc2hlZDsiLz48L3A+PHA+PHNwYW4gc3R5bGU9"+
						"ImNvbG9yOiM3ZjdmN2Y7Zm9udC1zaXplOjEycHg7Ij7nirbmgIHlkI3np7A6ICDorqLoiLHnoa7o"+
						"rqQ8QlIvPuiIquermeWQjeensDogIOWMl+S6rDxCUi8+6Iiq54+t5Y+3OiAgTVU1MTgzPEJSLz7m"+
						"l6XmnJ86ICAyMDEzLTEyLTEzPEJSLz7ml7bpl7Q6ICAxNjoyMDxCUi8+54q25oCB5o+P6L+wOiAg"+
						"6LSn54mp5bey6K6i5aal5oyH5a6a6Iiq54+t55qE6Iix5L2NLOiuoeWIkuS7jiDljJfkuqwg6L+Q"+
						"5b6AIOS4iua1t+a1puS4nDxCUi8+5Lu25pWwOiAgNDxCUi8+6YeN6YePOiAgMjEwMDwvc3Bhbj48"+
						"L3A+PHA+PGhyIHN0eWxlPSJjb2xvcjojZDhkOGQ4OyB3aWR0aDo5OCU7Ym9yZGVyLXN0eWxlOiBk"+
						"YXNoZWQ7Ii8+PC9wPg==</content><url>http://m.eft.cn/waybill/index.html?mawbcode=112-04220996*1118130022*5959739642627030448</url><imgurl>http://mmsns.qpic.cn/mmsns/LUkW1zMiauPqeaxFpQickX3yCNiaX74Bp1Yg4SzYlcPYgib5AdsAG48q5w/0</imgurl></messageitem><wxMsgId>5959739642627030448</wxMsgId><MsgType>RESEND</MsgType><ErrorMsgType>NORMAL</ErrorMsgType><openid>omQjGjvFW5Qtg4CmTj-toAjB8wTY</openid><stardard_data>状态名称:  到达<BR/>航站名称:  德里<BR/>航班号:  MU563<BR/>日期:  2013-12-23<BR/>时间:  00:33<BR/>状态描述:  货物和文件已由指定航班运达并被接收<BR/>件数:  4<BR/>重量:  2095</stardard_data><awb_code>112-04220996</awb_code></ServiceData></eFreightService>";
		/*
		String newxml = "<eFreightService>"+
				"	<ServiceURL>WXAPIService</ServiceURL>"+
				"	<ServiceAction>PushSyncTraceResult</ServiceAction>"+
				"	<ServiceData>"+
				"		<WXUserInfo>"+
				"			<openid/>"+
				"			<wxfakeid>3660475</wxfakeid>"+
				"			<awb_code>057-36956172</awb_code>"+
				"			<message>"+
				"				<TraceTranslates>"+
				"					<piece>1</piece>"+
				"					<weight>32.8</weight>"+
				"					<code>DEP</code>"+
				"					<base_info>"+
				"						<origin_airport>LNZ</origin_airport>"+
				"						<destination_airport>PEK</destination_airport>"+
				"						<current_status>2</current_status>"+
				"					</base_info>"+
				"					<routes>"+
				"						<route>"+
				"							<dep_port>LNZ</dep_port>"+
				"							<arr_port>CDG</arr_port>"+
				"							<flight_no>AF827</flight_no>"+
				"							<flight_date>2013-09-19 22:40</flight_date>"+
				"						</route>"+
				"						<route>"+
				"							<dep_port>CDG</dep_port>"+
				"							<arr_port>PEK</arr_port>"+
				"							<flight_no>AF382</flight_no>"+
				"							<flight_date>2013-09-21 23:16</flight_date>"+
				"						</route>"+
				"					</routes>"+
				"					<TraceTranslate>"+
				"						<AWB_CODE>057-36956172</AWB_CODE>"+
				"						<FLIGHT_NO>AF411</FLIGHT_NO>"+
				"						<FLIGHT_DATE>2013-09-18 05:00</FLIGHT_DATE>"+
				"						<SHIMPENT_PIECE>6</SHIMPENT_PIECE>"+
				"						<SHIMPENT_WEIGHT>1880</SHIMPENT_WEIGHT>"+
				"						<CARGO_CODE>BKD</CARGO_CODE>"+
				"						<CARGO_NAME>货物订舱</CARGO_NAME>"+
				"						<CARGO_ENNAME/>"+
				"						<TRACE_CODE>1057256</TRACE_CODE>"+
				"						<TRACE_TIME>2013-09-13 09:10</TRACE_TIME>"+
				"						<TRACE_LOCATION>LNZ</TRACE_LOCATION>"+
				"						<AIRPORT_DEP>LNZ</AIRPORT_DEP>"+
				"						<AIRPORT_LAND>CDG</AIRPORT_LAND>"+
				"						<ORIGIN_AIRPORT>LNZ</ORIGIN_AIRPORT>"+
				"						<DESTINATION_AIRPORT>PEK</DESTINATION_AIRPORT>"+
				"						<STARDARD_DATA>BKD 货物订舱: 预订航班AF411(时间 09-18 05:00 地点 LNZ 机场)，6件货物(重量为1880.0KG)</STARDARD_DATA>"+
				"						<TRACE_DATA>station:  LNZ&lt;BR/&gt;MSG:  BKD&lt;BR/&gt;received time:  13Sep13 09:10&lt;BR/&gt;PCS:  6&lt;BR/&gt;WGT:  1880k&lt;BR/&gt;other detail:  Rte:LNZ-KIX, Seg:LNZ-CDG, AF411M, Dep:18 SEP 05:00S, Arr:18 SEP 	19:58S, Summ:T6K1880, Dtl:T6K1880 </TRACE_DATA>"+
				"						<OP_TIME>2013-10-09 15:19</OP_TIME>"+
				"					</TraceTranslate>"+
				"					<TraceTranslate>"+
				"						<AWB_CODE>057-36956172</AWB_CODE>"+
				"						<FLIGHT_NO>AF292</FLIGHT_NO>"+
				"						<FLIGHT_DATE>2013-09-24 13:50</FLIGHT_DATE>"+
				"						<SHIMPENT_PIECE>6</SHIMPENT_PIECE>"+
				"						<SHIMPENT_WEIGHT>1880</SHIMPENT_WEIGHT>"+
				"						<CARGO_CODE>BKD</CARGO_CODE>"+
				"						<CARGO_NAME>货物订舱</CARGO_NAME>"+
				"						<CARGO_ENNAME/>"+
				"						<TRACE_CODE>1057257</TRACE_CODE>"+
				"						<TRACE_TIME>2013-09-13 10:01</TRACE_TIME>"+
				"						<TRACE_LOCATION>CDG</TRACE_LOCATION>"+
				"						<AIRPORT_DEP>CDG</AIRPORT_DEP>"+
				"						<AIRPORT_LAND>KIX</AIRPORT_LAND>"+
				"						<ORIGIN_AIRPORT>LNZ</ORIGIN_AIRPORT>"+
				"						<DESTINATION_AIRPORT>PEK</DESTINATION_AIRPORT>"+
				"						<STARDARD_DATA>BKD 货物订舱: 预订航班AF292(时间 09-24 13:50 地点 CDG 机场)，6件货物(重量为1880.0KG)</STARDARD_DATA>"+
				"						<TRACE_DATA>station:  CDG&lt;BR/&gt;MSG:  BKD&lt;BR/&gt;received time:  13Sep13 10:01&lt;BR/&gt;PCS:  6&lt;BR/&gt;WGT:  1880k&lt;BR/&gt;other detail:  Rte:LNZ-KIX, Seg:CDG-KIX, AF292, Dep:24 SEP 13:50S, Arr:25 SEP 08:25S, Summ:T6K1880, Dtl:T6K1880 </TRACE_DATA>"+
				"						<OP_TIME>2013-10-09 15:19</OP_TIME>"+
				"					</TraceTranslate>"+
				"					"+
				"				</TraceTranslates>"+
				"			</message>"+
				"			<wxMsgId></wxMsgId>"+
				"			<isError>0</isError>"+
				"			<MsgType>AWBTRACE</MsgType>"+
				"			<ErrorMsgType>NORMAL</ErrorMsgType>"+
				"		</WXUserInfo>"+
				"	</ServiceData>"+
				"</eFreightService>";
		
		String newxml = "<eFreightService>"+
				"<ServiceURL>WXAPIService</ServiceURL>"+
				"<ServiceAction>PushSubscribeTraceResult</ServiceAction>"+
				"<ServiceData>"+
				"<WXUserInfo>"+
				"<openid></openid>"+
				"<wxfakeid>3660475</wxfakeid>"+
				"<awb_code>999-12345675</awb_code>"+
				"<message>"+
				"<TraceTranslates>"+
				"<piece>8</piece>"+
				"<weight>511</weight>"+
				"<code>BKD</code>"+
				"<base_info>"+
				"<origin_airport></origin_airport>"+
				"<destination_airport></destination_airport>"+
				"<current_status>0</current_status>"+
				"</base_info>"+
				"<routes>"+
				"<route>"+
				"<dep_port>BJS</dep_port>"+
				"<arr_port>SVO</arr_port>"+
				"<flight_no>SU201</flight_no>"+
				"<flight_date>2013-08-07</flight_date>"+
				"</route>"+
				"<route>"+
				"<dep_port>SVO</dep_port>"+
				"<arr_port>EVN</arr_port>"+
				"<flight_no>SU1862</flight_no>"+
				"<flight_date>2013-08-11</flight_date>"+
				"</route>"+
				"</routes>"+
				"<TraceTranslate>"+
				"<AWB_CODE>074-11158033</AWB_CODE>"+
				"<FLIGHT_NO>SU201</FLIGHT_NO>"+
				"<FLIGHT_DATE>2013-08-07</FLIGHT_DATE>"+
				"<SHIMPENT_PIECE>8</SHIMPENT_PIECE>"+
				"<SHIMPENT_WEIGHT>511</SHIMPENT_WEIGHT>"+
				"<CARGO_CODE>BKD</CARGO_CODE>"+
				"<CARGO_NAME>货物订舱</CARGO_NAME>"+
				"<CARGO_ENNAME/>"+
				"<TRACE_CODE>1231794</TRACE_CODE>"+
				"<TRACE_TIME>2013-08-05 14:20</TRACE_TIME>"+
				"<TRACE_LOCATION>BJS</TRACE_LOCATION>"+
				"<AIRPORT_DEP>BJS</AIRPORT_DEP>"+
				"<AIRPORT_LAND>SVO</AIRPORT_LAND>"+
				"<ORIGIN_AIRPORT/>"+
				"<DESTINATION_AIRPORT/>"+
				"<STARDARD_DATA>BKD 货物订舱: 预订航班SU201(时间 08-07 地点 BJS 机场)，8件货物(重量为511.0KG)</STARDARD_DATA>"+
				"<TRACE_DATA>"+
				"Flight Date:  2013-08-07&lt;BR/&gt;Flight Number:  SU201&lt;BR/&gt;Itinerary:  \"BJS SVO&lt;BR/&gt;Number of Pieces:  8&lt;BR/&gt;Total Weight:  511&lt;BR/&gt;\"Status Date:  2013-08-05 14:20&lt;BR/&gt;Status:  Booked"+
				"</TRACE_DATA>"+
				"<OP_TIME>2013-08-06 13:59</OP_TIME>"+
				"</TraceTranslate>"+
				"<TraceTranslate>"+
				"<AWB_CODE>074-11158033</AWB_CODE>"+
				"<FLIGHT_NO>SU1862</FLIGHT_NO>"+
				"<FLIGHT_DATE>2013-08-11</FLIGHT_DATE>"+
				"<SHIMPENT_PIECE>8</SHIMPENT_PIECE>"+
				"<SHIMPENT_WEIGHT>511</SHIMPENT_WEIGHT>"+
				"<CARGO_CODE>BKD</CARGO_CODE>"+
				"<CARGO_NAME>货物订舱</CARGO_NAME>"+
				"<CARGO_ENNAME/>"+
				"<TRACE_CODE>1231795</TRACE_CODE>"+
				"<TRACE_TIME>2013-08-05 14:20</TRACE_TIME>"+
				"<TRACE_LOCATION>SVO</TRACE_LOCATION>"+
				"<AIRPORT_DEP>SVO</AIRPORT_DEP>"+
				"<AIRPORT_LAND>EVN</AIRPORT_LAND>"+
				"<ORIGIN_AIRPORT/>"+
				"<DESTINATION_AIRPORT/>"+
				"<STARDARD_DATA>BKD 货物订舱: 预订航班SU1862(时间 08-11 地点 SVO 机场)，8件货物(重量为511.0KG)</STARDARD_DATA>"+
				"<TRACE_DATA>"+
				"Flight Date:  2013-08-11&lt;BR/&gt;Flight Number:  SU1862&lt;BR/&gt;Itinerary:  \"SVO EVN&lt;BR/&gt;Number of Pieces:  8&lt;BR/&gt;Total Weight:  511&lt;BR/&gt;\"Status Date:  2013-08-05 14:20&lt;BR/&gt;Status:  Booked"+
				"</TRACE_DATA>"+
				"<OP_TIME>2013-08-06 13:59</OP_TIME>"+
				"</TraceTranslate>"+
				"</TraceTranslates>"+
				"</message>"+
				"<wxMsgId>5908881921252803901</wxMsgId>"+
				"<isError>0</isError>"+
				"<MsgType>SUBSCRIBE</MsgType>"+
				"<ErrorMsgType>PUSH</ErrorMsgType>"+
				"</WXUserInfo>"+
				"</ServiceData>"+
				"</eFreightService>";
		*/
		
//		newxml = "<eFreightService>"+
//				"<ServiceURL>WXAPIService</ServiceURL>"+
//				"<ServiceAction>PushSyncTraceResult</ServiceAction>"+
//				"<ServiceData>"+
//				"<WXUserInfo>"+
//				"<openid></openid>"+
//				"<wxfakeid>3660475</wxfakeid>"+
//				"<awb_code>999-12345675</awb_code>"+
//				"<message>运单695-14123874:网站暂无轨迹</message>"+
//				"<wxMsgId>5912226725818812854</wxMsgId>"+
//				"<isError>0</isError>"+
//				"<MsgType>AWBTRACE</MsgType>"+
//				"<ErrorMsgType>NORMAL</ErrorMsgType>"+
//				"</WXUserInfo>"+
//				"</ServiceData>"+
//				"</eFreightService>";
		
		String responseStr = HttpHandler.postHttpRequest("http://weixin.efreight.cn/WeixinService/WXAPIService", newxml);
		
//		newxml = "{\"action\":\"candy\",\"openid\":\"xxxxx\",\"awbcode\":\"999-12345675\"}";
//		String responseStr = HttpHandler.postHttpRequest("http://192.168.0.232:8080/NewWeixinService/HalloweenServlet", newxml);
		System.out.println(responseStr);
//		Test hpu = new Test();
//		try {
//			hpu.loginToWX();
//			// hpu.SendWXMessage();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String filepath = "/Users/xianan/Pictures/ozBfxjigVHhNjtBHOIwN8GtK0_nE_5881446915806454398.jpg";
//
////		 String urlStr = "http://115.28.35.46/WeixinService/WXFileUploadServlet";
//		 String urlStr = "http://127.0.0.1:8080/WeixinService/WXFileUploadServlet";
////		String urlStr = "&fid=10000123&fileid=10000123&error=false&tofakeid=348592560&token=1261411302&agax=1";
////		Map<String, String> textMap = new HashMap<String, String>();
//
////		textMap.put("name", "testname");
////
//		Map<String, String> fileMap = new HashMap<String, String>();
//		fileMap.put("userfile", filepath);
//		fileMap.put("userfile", filepath);
//
//		String ret = hpu.formUpload(urlStr, fileMap, fileMap);
//
//		System.out.println(ret);
//		System.out.println(token);

	}
	
	private static String ssss(String message,List<String> s){
		String returnMessage = "";
		try{
			String code = message.replaceAll("一", "1").replaceAll("壹", "1")
					.replaceAll("二", "2").replaceAll("贰", "2").replaceAll("三", "3")
					.replaceAll("叁", "3").replaceAll("四", "4").replaceAll("肆", "4")
					.replaceAll("五", "5").replaceAll("伍", "5").replaceAll("六", "6")
					.replaceAll("陆", "6").replaceAll("七", "7").replaceAll("柒", "7")
					.replaceAll("八", "8").replaceAll("捌", "8").replaceAll("九", "9")
					.replaceAll("玖", "9").replaceAll("零", "0");
			Pattern regex = Pattern.compile("\\D");
			Matcher matcher = regex.matcher(code);
			code = matcher.replaceAll("");
			message = message.trim();
			Pattern p = Pattern.compile("\\d{11}");  
			Matcher m = p.matcher(code);  
			if(m.matches()){//判断11位运单
				returnMessage = "TRACE|asnycProcess|TRACE";
				s.add(code);
			}
		}catch(Exception e){
			System.out.println("ssss");
		}
		return returnMessage;
	}

	protected boolean loginToWX() throws Exception {
		StringBuilder responseData = new StringBuilder();
		try {
			HttpURLConnection request = this.GetRequest(LOGINURL, "POST");
			String requestXml = "username=" + USERNAME + "&pwd="
					+ CreateMD5(PASSWORD) + "&imgcode=&f=json";
			request.setRequestProperty("Content-Length",
					String.valueOf(requestXml.getBytes().length));

			request.connect();
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					request.getOutputStream(), "UTF-8"));
			out.write(requestXml);// 要post的数据，多个以&符号分割
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					(InputStream) request.getInputStream()));
			String line = null;

			while ((line = in.readLine()) != null) {
				responseData.append(line);
			}
			in.close();
			GetCookies(request);
		} catch (Exception e) {
			throw new Exception("登陆微信网站失败：" + e.getMessage());
		}
		String responseStr = responseData.toString();
		int index = responseStr.indexOf("token=");
		token = responseStr.substring(index + 6,
				responseStr.indexOf("\"", index));

		return responseData.toString().contains("\"Ret\": 302");
	}

	private void GetCookies(HttpURLConnection request) {
		List<String> cookieList = request.getHeaderFields().get("Set-Cookie");
		if (cookieList == null)
			return;
		for (String cookie : cookieList) {
			String cookieKeyAndValue = cookie.split(";")[0];
			int position = cookieKeyAndValue.indexOf("=");
			String key = cookieKeyAndValue.substring(0, position);
			String value = cookieKeyAndValue.substring(position + 1);
			cookies.put(key, value);
		}
	}

	private String CreateMD5(String str) {
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
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}

		return md5StrBuff.toString();
	}

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

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	/**
	 * 
	 * 获取HTTPURLCONNECTION对象，并设置COOKIE
	 */
	private HttpURLConnection GetRequest(String urlstring, String requestmethod) {
		HttpURLConnection request = null;
		// Properties systemProperties = System.getProperties();
		// systemProperties.setProperty("http.proxyHost", "127.0.0.1");
		// systemProperties.setProperty("http.proxyPort", "8888");
		try {
//			SSLContext sc = SSLContext.getInstance("SSL");
//			sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
//					new java.security.SecureRandom());
			URL url = new URL(urlstring);
			request = (HttpURLConnection) url.openConnection();
//			request.setSSLSocketFactory(sc.getSocketFactory());
//			request.setHostnameVerifier(new TrustAnyHostnameVerifier());

			request.setRequestMethod(requestmethod.toUpperCase());

			request.setDoInput(true);
			request.setDoOutput(true);
			request.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded; charset=UTF-8");
			request.setRequestProperty("Accept",
					"application/json, text/javascript, */*; q=0.01");
			request.setRequestProperty(
					"Referer",
					"https://mp.weixin.qq.com/cgi-bin/indexpage?token="
							+ token
							+ "&lang=zh_CN&t=wxm-upload&lang=zh_CN&type=0&fromId=file_from_1341151893625");
			request.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.97 Safari/537.22");
			request.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.3");
			request.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			request.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			this.SetCookies(request);
			// request.connect();
			// foreach (Cookie cookie in response.Cookies)
			// {
			// cookieContainer.Add(cookie);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	private void SetCookies(HttpURLConnection request) {
		String cookiestring = "remember_acct=efreight; hasWarningUser=1;";
		for (String key : cookies.keySet()) {
			cookiestring += key + "=" + cookies.get(key) + "; ";
		}
		if (cookiestring.length() > 0)
			request.setRequestProperty("Cookie", cookiestring);
	}

	/**
	 * 发送文本消息
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String SendWXMessage() throws Exception {
		String wxid = "348592560";
		String message = "12345";
		boolean success = this.SendWXTextMessage(wxid, message);
		// 起线程发log
		return success ? "发送成功" : "发送失败";
	}

	public boolean SendWXTextMessage(String fakeid, String messagestr)
			throws Exception {
		String requestData = "type=1&content="
				+ URLEncoder.encode(messagestr, "UTF-8")
				+ "&error=false&tofakeid=" + fakeid + "&ajax=1&token=" + token;
		String responseData = this
				.GetPostHttpResponseString(
						"https://mp.weixin.qq.com/cgi-bin/singlesend?t=ajax-response&lang=zh_CN",
						requestData);

		if (!responseData.contains("\"msg\":\"ok\"")) {
			return false;
		}
		return true;
	}

	private String GetPostHttpResponseString(String url, String requestData)
			throws Exception {
		HttpURLConnection request = this.GetRequest(url, "POST");

		request.setRequestProperty("Content-Length",
				String.valueOf(requestData.getBytes("UTF-8").length));
		request.connect();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				request.getOutputStream(), "UTF-8"));
		out.write(requestData);// 要post的数据，多个以&符号分割
		out.flush();
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				(InputStream) request.getInputStream()));
		String line = null;
		StringBuilder responseData = new StringBuilder();
		while ((line = in.readLine()) != null) {
			responseData.append(line);
		}
		in.close();
		// System.out.println("发送请求到URL：" + url+",数据：" + requestData + ",结果：" +
		// responseData.toString());
		return responseData.toString();
	}

	/**
	 * 上传图片
	 * 
	 * @param urlStr
	 * @param textMap
	 * @param fileMap
	 * @return String
	 */
	public String formUpload(String urlStr, Map<String, String> textMap,
			Map<String, String> fileMap) {
		String res = "";
		HttpURLConnection conn = null;
		String BOUNDARY = "---------------------------123821742118716"; // boundary就是request头和上传文件内容的分隔符
		try {
//			SSLContext sc = SSLContext.getInstance("SSL");
//			sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
//					new java.security.SecureRandom());
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
//			conn.setSSLSocketFactory(sc.getSocketFactory());
//			conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
			conn.setRequestProperty("Host", "mp.weixin.qq.com");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty(
					"Referer",
					"https://mp.weixin.qq.com/cgi-bin/indexpage?token="
							+ token
							+ "&lang=zh_CN&t=wxm-upload&lang=zh_CN&type=0&fromId=file_from_1341151893625");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20100101 Firefox/20.0");
			// conn.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.3");
			conn.setRequestProperty("Accept-Language",
					"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			this.SetCookies(conn);
			conn.setRequestProperty("Connection", "Keep-Alive");

			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(conn.getOutputStream());

			// file
			if (fileMap != null) {
				Iterator iter = fileMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					File file = new File(inputValue);
					String filename = file.getName();
					String contentType = new MimetypesFileTypeMap()
							.getContentType(file);
					if (filename.endsWith(".png")) {
						contentType = "image/png";
					}
					if (contentType == null || contentType.equals("")) {
						contentType = "application/octet-stream";
					}

					StringBuffer strBuf = new StringBuffer();
					strBuf.append("\r\n").append("--").append(BOUNDARY)
							.append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
							+ filename + "\"\r\n");
					strBuf.append("Content-Type:" + contentType + "\r\n\r\n");

					out.write(strBuf.toString().getBytes());

					DataInputStream in = new DataInputStream(
							new FileInputStream(file));
					int bytes = 0;
					byte[] bufferOut = new byte[1024];
					while ((bytes = in.read(bufferOut)) != -1) {
						out.write(bufferOut, 0, bytes);
					}
					in.close();
				}
			}

			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			// 读取返回数据
			StringBuffer strBuf = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "utf-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			reader.close();
			reader = null;
		} catch (Exception e) {
			System.out.println("发送POST请求出错。" + urlStr);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

}