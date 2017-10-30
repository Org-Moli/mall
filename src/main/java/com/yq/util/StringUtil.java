package com.yq.util;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.change.util.PageData;

public class StringUtil {
	
	public static String success_message = "提交成功" ;
	public static String error_message = "提交失败" ;
	public static String except_message = "服务器异常" ;
	public static String max_message = "已超出数量" ;
	public static String had_message = "您已经领取过啦" ;
	
	
	public static PageData shopUser(HttpSession session){ //获取用户信息
//		setShopUser(session);//模拟用户信息
		return  (PageData) session.getAttribute("shopUser");
	}
	
	public static void setShopUser(HttpSession session){ //模拟用户信息
		PageData shopUser = new PageData();
		shopUser.put("user_id", "16");
		shopUser.put("phone", "13800138000");
		shopUser.put("username", "千派网络");
		shopUser.put("openid", "o6asAw05iVuIlqSgWIi3GpYbc72M");
		session.setAttribute("cart_count",5);
		session.setAttribute("shopUser",shopUser);
	}
	public static String getId(){
		//int random_num = (int) ((Math.random() * 9 + 1) * 100000);
		return new Date().getTime()+"";
	}
	
	
	public static int toInt(String str){
		int result = 0;
		if(str.contains(".")){
			result = Integer.parseInt(str.split("\\.")[0]);
		}else{
			result = Integer.parseInt(str);
		}
		return result;
	}

}
