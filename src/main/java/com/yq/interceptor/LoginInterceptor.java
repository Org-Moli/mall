package com.yq.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.change.util.MD5;
import org.change.util.PageData;
import com.google.gson.Gson;
import com.weixin.util.Config;
import com.yq.service.cart.CartManager;
import com.yq.service.user.ShopUserManager;
import com.yq.util.CookieUtil;

/**
 * 登陆拦截器.
 *
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
	@Resource(name = "shopUserService")
	private ShopUserManager shopUserService;
	@Resource(name = "cartService")
	private CartManager cartService;
	private Gson gson = new Gson();
	private Logger log = Logger.getLogger(this.getClass());
	Map<String, Object> map = new HashMap<String, Object>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession();
		boolean flag = false;
		String param = request.getQueryString();
		String url = "";
		System.out.println(param);
		if(StringUtils.isEmpty(param)){
			url = request.getRequestURL().toString();
		}else{
			url = (request.getRequestURL() + "?" + request.getQueryString()).toString();
		}
		flag = session.getAttribute("shopUser") != null ? true : false;
		if (!flag) {
//			if (login(request, response)) {//判断用户的登录cookie是否存在
//				response.sendRedirect(url);
//			} else {
				if (request.getHeader("x-requested-with") != null
						&& request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) { // 如果是ajax请求响应头会有x-requested-with
					log.info("ajax -- 进入--->>>");
					map.put("url", url);
					map.put("result", 1005);
					map.put("message", "尚未登录，跳转中...");
					response.setContentType("text/json;charset=utf-8");
					response. setCharacterEncoding("UTF-8");
					response.getWriter().write(gson.toJson(map));
				} else {
					log.info("不是ajax -- 进入--->>>");
					// response.sendRedirect("/app/to_login");
					log.info("请求url--->>>"+url);
					
					PageData config = Config.getconfig();
					response.sendRedirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + config.getString("appid")
							+ "&redirect_uri=" + config.getString("link") + "/app/weixinlogin?url=" + url
							+ "&response_type=code&scope=snsapi_userinfo&state=STATE&connect_redirect=1#wechat_redirect");
					
					
//					request.setAttribute("url", url);
//					request.getRequestDispatcher("/app/to_login").forward(request, response);

//				}
			}
		}
		// flag=true ;
		return flag;

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
	//微信内没有cookie，所以舍去（微信应用退出后，cookie会被自动清除）
//	public boolean login(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		boolean result = false;
//		PageData pd = new PageData();
//		String phone = "";
//		String password = "";
//		String openid = "";
//		String weixin = "";
//		
//		Cookie[] cookies = request.getCookies();
//		if (cookies != null && cookies.length > 0) { // 如果没有设置过Cookie会返回null
//			for (Cookie cookie : cookies) {
//				// 微信登录的cookie
//				if ("weixin".equals(cookie.getName())) {
//					weixin = cookie.getValue();
//				}
//				
//				if ("openid".equals(cookie.getName())) {
//					openid = cookie.getValue();
//				}
//				// 手机号登录的cookie
//				if ("phone".equals(cookie.getName())) {
//					phone = cookie.getValue();
//				}
//				if ("password".equals(cookie.getName())) {
//					password = cookie.getValue();
//				}
//
//			}
//			if (StringUtils.isNotEmpty(weixin)) {
//				pd.put("openid", openid);
//				PageData shopUser = shopUserService.findById(pd);
//				if (shopUser != null) {//存在用户
//					result = true;
////					CookieUtil.cookieadd(response, "openid", openid);
//					request.getSession().setAttribute("shopUser", shopUser);
//					pd.put("user_id", shopUser.getString("user_id"));
//					request.getSession().setAttribute("cart_count", cartService.count(pd));
//				}
//				
//			} else if (StringUtils.isNotEmpty(phone)) {
//				pd.put("phone", phone);
//				pd.put("password", MD5.md5(password));
//				PageData shopUser = shopUserService.findByPhone(pd);
//				if (shopUser != null) {// 账号密码正确
//					result = true;
////					CookieUtil.cookieadd(response, "phone", phone);
////					CookieUtil.cookieadd(response, "password", password);
//					shopUser.put("openid", openid);
//					request.getSession().setAttribute("shopUser", shopUser);
//					pd.put("user_id", shopUser.getString("user_id"));
//					request.getSession().setAttribute("cart_count", cartService.count(pd));
//				}
//			}
//		}
//		return result;
//	}
}