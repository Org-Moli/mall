package com.yq.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.alibaba.fastjson.JSON;

/**
 *
 * @param url
 *            应用地址，类似于http://ip:port/msg/
 * @param un
 *            账号
 * @param pw
 *            密码
 * @param phone
 *            手机号码，多个号码使用","分割
 * @param msg
 *            短信内容
 * @param rd
 *            是否需要状态报告，需要1，不需要0
 * @return 返回值定义参见HTTP协议文档
 * @throws Exception
 */
public class SmsUtil {
	public static String batchSend(String url, String un, String pw, String phone, String msg, String rd, String ex)
			throws Exception {
		HttpClient client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true));
		GetMethod method = new GetMethod();
		try {
			URI base = new URI(url, false);
			method.setURI(new URI(base, "send", false));
			method.setQueryString(new NameValuePair[] { new NameValuePair("un", un), new NameValuePair("pw", pw),
					new NameValuePair("phone", phone), new NameValuePair("rd", rd), new NameValuePair("msg", msg),
					new NameValuePair("ex", ex), });
			int result = client.executeMethod(method);
			if (result == HttpStatus.SC_OK) {
				InputStream in = method.getResponseBodyAsStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				return URLDecoder.decode(baos.toString(), "UTF-8");
			} else {
				throw new Exception("HTTP ERROR Status: " + method.getStatusCode() + ":" + method.getStatusText());
			}
		} finally {
			method.releaseConnection();
		}
	}

//		public static void main(String[] args) {
//
//			String url = "http://smssh1.253.com/msg/send/json"; //"http://sms.253.com/msg/"// 应用地址
//			String un = "N7374241";// 账号
//			String pw = "rOVbLQ76io18e2";// 密码
//			String phone = "15722448773";// 手机号码，多个号码使用","分割
//			String msg = "【你的签名】您好，你的验证码是123456";// 短信内容
//			String rd = "0";// 是否需要状态报告，需要1，不需要0
//			String ex = null;// 扩展码
//
//			try {
//				String returnString = batchSend(url, un, pw, phone, msg, rd, ex);
//				System.out.println(returnString);
//				// TODO 处理返回值,参见HTTP协议文档
//			} catch (Exception e){
//				// TODO 处理异常
//				e.printStackTrace();
//			}
//		}
	
	public static final String charset = "utf-8";
        // 用户平台API账号(非登录账号,示例:N1234567)
        public static String account = "N7374241";
        // 用户平台API密码(非登录密码)
        public static String pswd = "rOVbLQ76io18e2";

        public static void main(String[] args) throws UnsupportedEncodingException {

                //请求地址请登录253云通讯自助通平台查看或者询问您的商务负责人获取
                String smsSingleRequestServerUrl = "http://smssh1.253.com/msg/send/json";
                // 短信内容
                String msg = "【安e购】你好,你的验证码是123456";
                //手机号码
                String phone = "13951111277,18521529110";
                //状态报告
                String report= "true";
                
                SmsSendRequest smsSingleRequest = new SmsSendRequest(account, pswd, msg, phone,report);
                
                String requestJson = JSON.toJSONString(smsSingleRequest);
                
                System.out.println("before request string is: " + requestJson);
                
                String response = sendSmsByPost(smsSingleRequestServerUrl, requestJson);
                
                System.out.println("response after request result is :" + response);
                
                SmsSendResponse smsSingleResponse = JSON.parseObject(response, SmsSendResponse.class);
                
                System.out.println("response  toString is :" + smsSingleResponse);
                
        
        }
        
        public static String sendSmsByPost(String path, String postContent) {
            URL url = null;
            try {
                    url = new URL(path);
                    System.out.println(url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");// 提交模式
                    httpURLConnection.setConnectTimeout(10000);//连接超时 单位毫秒
                    httpURLConnection.setReadTimeout(10000);//读取超时 单位毫秒
                    // 发送POST请求必须设置如下两行
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestProperty("Charset", "UTF-8");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");

                    httpURLConnection.connect();
                    OutputStream os=httpURLConnection.getOutputStream();
                    os.write(postContent.getBytes("UTF-8"));
                    os.flush();
                    
                    StringBuilder sb = new StringBuilder();
                    int httpRspCode = httpURLConnection.getResponseCode();
                    if (httpRspCode == HttpURLConnection.HTTP_OK) {
                            // 开始获取数据
                            BufferedReader br = new BufferedReader(
                                            new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                            String line = null;
                            while ((line = br.readLine()) != null) {
                                    sb.append(line);
                            }
                            br.close();
                            return sb.toString();

                    }

            } catch (Exception e) {
                    e.printStackTrace();
            }
            return null;
    }
	
}