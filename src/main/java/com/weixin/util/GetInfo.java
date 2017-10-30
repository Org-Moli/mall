package com.weixin.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.change.util.PageData;

public class GetInfo {
	// @Autowired
	// private static UserInfoService userInfoService = new UserInfoService();
	private static Logger log = Logger.getLogger(GetInfo.class);

	@SuppressWarnings("unchecked")
	public static Map<String, String> parseXml(HttpServletRequest request) {
		try {
			// 将解析结果存储在HashMap中
			Map<String, String> map = new HashMap<String, String>();

			// 从request中取得输入流
			InputStream inputStream = request.getInputStream();
			// 读取输入流
			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStream);
			// 得到xml根元素
			Element root = document.getRootElement();
			// 得到根元素的所有子节点
			List<Element> elementList = root.elements();

			// 遍历所有子节点
			for (Element e : elementList)
				map.put(e.getName(), e.getText());

			// 释放资源
			inputStream.close();
			inputStream = null;
			return map;
		} catch (Exception e) {
			return null;
		}
	}

	public static PageData baseInfo(PageData pd,HttpServletRequest request) throws Exception {
		String openid = "";
//		String unionid = "";
		String access_token = "";
		PageData config = Config.getconfig();
		String code = pd.getString("code");// 获取code值
		log.info("通过确认授权获取信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info(pd);
		if (code != null) {
			String token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + config.getString("appid")
					+ "&secret=" + config.getString("appsecret") + "&code=" + code + "&grant_type=authorization_code";
			System.out.println(token_url);
			// 获取用户的openid
			JSONObject json = new JSONObject();
			// CommonUtil commonUtil=new CommonUtil();
			json = Accesstoken.httpsRequest(token_url, "GET", null);
			log.info("通过code获取信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			log.info(json);
			if (json != null) {
				openid = json.getString("openid");
				access_token = json.getString("access_token");
			//	unionid = json.getString("unionid");
			}
		}
		pd.put("openid", openid);
	//	pd.put("unionid", unionid);
		pd.put("access_token", access_token);
		log.info("code==" + code);
		log.info("WXUTIL 96 --oppen_id==" + openid);
		return pd;
	}

	public static PageData userInfo(PageData pd ,HttpServletRequest request) {
		try {
			PageData baseInfo = baseInfo(pd,request);
			String openid = baseInfo.getString("openid");
			String access_token = baseInfo.getString("access_token");
	//		String unionid = baseInfo.getString("unionid");
			
			PageData userInfo = AdvancedUtil.getUserInfo(access_token, openid);
			log.info("--------------------");
			log.info("通过access_token获取 userInfo=" + userInfo);
			userInfo.put("openid", openid);
//			userInfo.put("unionid", unionid);
			return userInfo;
		} catch (Exception e) {
			e.getStackTrace();
			log.info("--------------------");
			log.info("获取用户信息出错 ---> " + e.getMessage());
			return null;
		}

	}

}
