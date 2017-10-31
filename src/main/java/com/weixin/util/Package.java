package com.weixin.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.change.util.PageData;

public class Package{
	private static Logger log = Logger.getLogger(Package.class);
	static HttpServletRequest request;
	static HttpServletResponse response;

	public static PageData getPackage(HttpSession session,Map<String, Object> map) {
		PageData config = Config.getconfig();
		String appid = config.getString("appid");
		String appsecret = config.getString("appsecret");
		String partner = config.getString("partner");
		String partnerkey = config.getString("partnerkey");
		String link =  config.getString("link");
		String timestamp = (String) map.get("timestamp");
		String nonceStr = (String) map.get("nonceStr");
		// 商品描述根据情况修改
		String body = (String) map.get("goods_name");
		BigDecimal price = (BigDecimal)map.get("order_total");
		System.out.println("价格："+map.get("order_total")+";price:"+price);
		String total_fee  = (price.multiply(new BigDecimal(100))).intValue()+"";
		System.out.println(total_fee);
		String openid = (String) map.get("openid");
		if(openid==null){
			PageData baseInfo = (PageData)session.getAttribute("baseInfo");
			if(baseInfo!=null){
				openid =baseInfo.getString("baseInfo");
			}
		}
		//String body = goods_name;
		String notify_url = link + "/weixin/return";
		log.info("notify_url=" + notify_url);
		// 附加数据
		String attach = "";
		// 商户订单号
		String out_trade_no = (String) map.get("order_id");

		String spbill_create_ip = "127.0.0.1";
		// request.getRemoteAddr();

		String trade_type = "JSAPI";
		// String openid = openId;
		// 非必输
		// String product_id = "";
		SortedMap<String, Object> packageParams = new TreeMap<String, Object>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", partner);
		packageParams.put("nonce_str", nonceStr);
		packageParams.put("body", body);
		packageParams.put("attach", attach);
		packageParams.put("out_trade_no", out_trade_no);

		packageParams.put("total_fee", total_fee);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notify_url);
		packageParams.put("trade_type", trade_type);
		packageParams.put("openid", openid);

		RequestHandler reqHandler = new RequestHandler(request, response);
		reqHandler.init(appid, appsecret, partnerkey);
		String sign = reqHandler.createSign(packageParams);
		System.out.println(sign);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + partner + "</mch_id>" + "<nonce_str>"
				+ nonceStr + "</nonce_str>" + "<sign>" + sign + "</sign>" + "<body><![CDATA[" + body + "]]></body>"
				+ "<attach>" + attach + "</attach>" + "<out_trade_no>" + out_trade_no + "</out_trade_no>" +
				// 金额，这里写的1 分到时修改
				"<total_fee>" + total_fee + "</total_fee>" + "<spbill_create_ip>" + spbill_create_ip
				+ "</spbill_create_ip>" + "<notify_url>" + notify_url + "</notify_url>" + "<trade_type>" + trade_type
				+ "</trade_type>" 
				+ "<openid>" + openid + "</openid>" 
				+ "</xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		String prepay_id = "";
		prepay_id = GetWxOrderno.getPayNo(createOrderURL, xml);
		System.out.println("prepay_id:"+prepay_id);
		SortedMap<String, Object> finalpackage = new TreeMap<String, Object>();
		String packages = "prepay_id=" + prepay_id;
		log.info("packages=" + packages);
		finalpackage.put("appId", appid);
		finalpackage.put("timeStamp", timestamp);
		finalpackage.put("nonceStr", nonceStr);
		finalpackage.put("package", packages);
		finalpackage.put("signType", "MD5");
		String finalsign = reqHandler.createSign(finalpackage);
		PageData pk = new PageData();
		pk.put("appId", appid);
		pk.put("timeStamp", timestamp);
		pk.put("nonceStr", nonceStr);
		pk.put("package", packages);
		pk.put("signType", "MD5");
		pk.put("paySign", finalsign);
		pk.put("prepay_id", prepay_id);
		return pk;

	}


	public HttpServletRequest getRequest() {
		return request;
	}

	

	public HttpServletResponse getResponse() {
		return response;
	}


}
