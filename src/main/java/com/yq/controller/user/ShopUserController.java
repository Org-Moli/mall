package com.yq.controller.user;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yq.entity.ResultBean;
import com.yq.service.msg.PhoneCaptchaManage;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import org.change.controller.base.BaseController;
import org.change.util.MD5;
import org.change.util.PageData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.weixin.util.Config;
import com.weixin.util.GetInfo;
import com.yq.service.cart.CartManager;
import com.yq.service.user.ShopUserManager;
import com.yq.util.CookieUtil;
import com.yq.util.DatetimeUtil;
import com.yq.util.SmsSendRequest;
import com.yq.util.SmsSendResponse;
import com.yq.util.SmsUtil;
import com.yq.util.StringUtil;

/**
 * 说明：会员用户 创建人： qq 357788906 创建时间：2016-12-28
 */
@Controller
public class ShopUserController extends BaseController {

    @Resource(name = "shopUserService")
    private ShopUserManager shopUserService;

    @Resource(name = "cartService")
    private CartManager cartService;

    @Resource(name = "phoneCaptchaService")
    private PhoneCaptchaManage phoneCaptchaService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    private Logger log = Logger.getLogger(this.getClass());

    @RequestMapping(value = "/app/to_wxlogin")
    public void to_wxlogin(HttpServletResponse response) throws Exception
    {
        PageData pd = new PageData();
        pd = this.getPageData();
        String url = pd.getString("url");
        PageData config = Config.getconfig();
        response.sendRedirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + config.getString("appid")
                + "&redirect_uri=" + config.getString("link") + "/app/to_login?url=" + url
                + "&response_type=code&scope=snsapi_base&state=STATE&connect_redirect=1#wechat_redirect");
        // this.getRequest().getRequestDispatcher("").forward(this.getRequest(),
        // response);
    }

    @RequestMapping(value = "/app/to_login")
    public ModelAndView to_login() throws Exception
    {
        PageData pd = new PageData();
        pd = this.getPageData();
        ModelAndView mv = new ModelAndView();
        pd.put("url", this.getRequest().getAttribute("url"));
        log.info("app/to_login-->" + pd);
        mv.addObject("pd", pd);
        mv.setViewName("center/user/login");
        return mv;
    }

    @RequestMapping(value = "/app/to_regist")
    public ModelAndView to_regist() throws Exception
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("center/user/regist");
        return mv;
    }

    @RequestMapping(value = "/app/to_forget")
    public ModelAndView to_forget() throws Exception
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("center/user/forget");
        return mv;
    }

    /**
     * 退出
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login_out")
    public ModelAndView login_out(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws Exception
    {
        ModelAndView mv = new ModelAndView();
        session.removeAttribute("shopUser");
        session.removeAttribute("cart_count");
        CookieUtil.cookiedelete(request, response);
        mv.setViewName("redirect:app/center/index");
        return mv;
    }

    @RequestMapping(value = "/user/info")
    public ModelAndView user_info() throws Exception
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("center/user/info");
        return mv;
    }

    @RequestMapping(value = "/user/username")
    public ModelAndView username() throws Exception
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("center/user/username");
        return mv;
    }

    @RequestMapping(value = "/user/phone")
    public ModelAndView phone() throws Exception
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("center/user/phone");
        return mv;
    }

    /**
     * 用户注册
     *
     * @param
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/app/regist", produces = "application/json;charset=UTF-8")
    public String regist(HttpServletResponse response) throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        int result = 0;
        String message = "";
        PageData pd = new PageData();
        pd = this.getPageData();
        String phone = pd.getString("phone");
        String code = pd.getString("code");
        String password = pd.getString("password");
        Integer captchaId = Integer.valueOf(pd.getString("captchaId"));
        pd.put("password", null);
        PageData isUser = shopUserService.findByPhone(pd);
        pd.put("password", password);
        if (isUser == null)
        {
            if (phoneCaptchaService.checkYzm(captchaId, code, phone))
            {
                // 验证短信
                if (checkpassword())
                { // 验证两次输入的密码
                    PageData baseInfo = (PageData) this.getRequest().getSession().getAttribute("baseInfo");
                    if (baseInfo != null)
                    {
                        pd.put("openid", baseInfo.getString("openid"));
                    }
                    pd.put("password", MD5.md5(pd.getString("password")));
                    pd.put("addtime", DatetimeUtil.getDatetime());
                    pd.put("head_img", "static/upload/headimg.jpg");
                    pd.put("username", "商城用户" + phone);
                    pd.put("user_id", this.get32UUID());
                    shopUserService.save(pd);
//					PageData shopUser = pd;// shopUserService.findByPhone(pd);
//					CookieUtil.cookieadd(response, "phone", phone);
//					CookieUtil.cookieadd(response, "password", password);
//					this.getRequest().getSession().setAttribute("shopUser", shopUser);
//					this.getRequest().getSession().setAttribute("cart_count", 0);
//					map.put("shopUser", shopUser);
                    result = 1;
                    message = "注册成功！";
                } else
                {
                    result = 0;
                    message = "两次密码不一致！";
                }
            } else
            {
                result = 0;
                message = "验证码错误！";
            }
        } else
        {
            result = 0;
            message = "用户已存在，请重新输入！";
        }

        map.put("result", result);
        map.put("message", message);
        return gson.toJson(map);
    }

    @RequestMapping(value = "/app/to_checklogin")
    public void to_checklogin(HttpServletResponse response) throws Exception
    {
        PageData pd = new PageData();
        pd = this.getPageData();
        log.info("196 . pd = " + pd);
        String path = this.getRequest().getContextPath();
        String url = this.getRequest().getScheme() + "://" + this.getRequest().getServerName() + ":" + this.getRequest().getServerPort() + path + "/index";

//		String url =  "index";//pd.getString("url");
        // url = (this.getRequest().getRequestURL() + "?" +
        // this.getRequest().getQueryString()).toString();
        String phone = pd.getString("phone");
        String password = pd.getString("password");
        log.info("196 -> url = " + url);
        String param = "{url:\"" + url + "\",phone:\"" + phone + "\",password:\"" + password + "\"}";
        log.info("204 -> param = " + param);
        String ua = this.getRequest().getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0)
        {// 是微信浏览器
            PageData config = Config.getconfig();
            String resUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + config.getString("appid")
                    + "&redirect_uri=" + config.getString("link") + "/app/login?param=" + param
                    + "&response_type=code&scope=snsapi_base&state=STATE&connect_redirect=1#wechat_redirect";
            log.info("203 . resUrl = " + resUrl);
            response.sendRedirect(resUrl);
        } else
        {
            log.info("216 -> param = " + param);
            this.getRequest().getRequestDispatcher("login?param=" + param).forward(this.getRequest(), response);
        }
    }

    /**
     * 用户登录
     *
     * @return
     */
    @RequestMapping(value = "/app/login")
    public void login(HttpServletResponse response) throws Exception
    {
        int result = 0;
        String message = "";
        PageData pd = new PageData();
        pd = this.getPageData();
        log.info("223 -> pd = " + pd);
        String param = pd.getString("param");
        log.info("226 -> param = " + param);
        JsonObject jsondata = new JsonParser().parse(param).getAsJsonObject();
        // jsondata.get("");
        String url = jsondata.get("url").getAsString();
        String phone = jsondata.get("phone").getAsString();
        log.info("225 -> phone = " + phone);
        if (StringUtils.isNotEmpty(phone))
        {
            String password = jsondata.get("password").getAsString();
            if (StringUtils.isEmpty(password))
            {
                pd.put("password", " ");
            } else
            {
                pd.put("password", MD5.md5(password));
            }
            pd.put("phone", phone);
            PageData shopUser = shopUserService.findByPhone(pd);
            pd.put("password", null);
            PageData isUser = shopUserService.findByPhone(pd);
            if (isUser != null)
            { // 用户存在
                if (shopUser != null)
                {// 账号密码正确
                    result = 1;
                    message = "登录成功！";
                    if (pd.getString("code") != null)
                    {
                        PageData baseInfo = GetInfo.baseInfo(pd, this.getRequest());
                        String openid = baseInfo.getString("openid");
                        shopUser.put("openid", openid);
                        CookieUtil.cookieadd(response, "openid", openid);
                    }
                    CookieUtil.cookieadd(response, "phone", phone);
                    CookieUtil.cookieadd(response, "password", password);
                    this.getRequest().getSession().setAttribute("shopUser", shopUser);
                    pd.put("user_id", shopUser.getString("user_id"));
                    this.getRequest().getSession().setAttribute("cart_count", cartService.count(pd));
                } else
                {
                    result = 0;
                    message = "密码错误！";
                }
            } else
            {
                result = -1;
                message = "用户不存在，请注册！";
            }

        } else
        {
            result = -2;
            message = "请输入手机号！";
        }
        if (result == 1)
        {
//			String path = this.getRequest().getContextPath();
//			url = this.getRequest().getScheme()+"://"+this.getRequest().getServerName()+":"+this.getRequest().getServerPort()+path+"/" +url;
            response.sendRedirect(url);
        } else
        {
            response.sendRedirect("to_login?url=" + url + "&result=" + result);
        }
    }

    // /**
    // * 微信登录
    // *
    // * @return
    // * @throws Exception
    // */
    // @ResponseBody
    // @RequestMapping(value = "/app/wxlogin", produces =
    // "application/json;charset=UTF-8")
    // public String appwxlogin() throws Exception {
    // Map<String, Object> map = new HashMap<String, Object>();
    // int result = 0;
    // String message = "";
    // PageData pd = new PageData();
    // pd = this.getPageData();
    // PageData isUser = shopUserService.findById(pd);
    // if (isUser != null) { // 用户存在
    // result = 1;
    // message = "登录成功！";
    //
    // this.getRequest().getSession().setAttribute("shopUser", isUser);
    //
    // } else {
    // result = 0;
    // message = "请填写手机信息！";
    // }
    // map.put("shopUser", isUser);
    // map.put("result", result);
    // map.put("message", message);
    // return gson.toJson(map);
    // }

    /**
     * 微信登录
     *
     * @return
     * @throws IOException
     * @throws Exception
     */
    @RequestMapping(value = "/app/wxlogin")
    public void wxlogin(HttpServletResponse response) throws IOException
    {
        PageData pd = new PageData();
        pd = this.getPageData();
        String url = pd.getString("url");
        PageData config = Config.getconfig();
        response.sendRedirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + config.getString("appid")
                + "&redirect_uri=" + config.getString("link") + "/app/weixinlogin?url=" + url
                + "&response_type=code&scope=snsapi_userinfo&state=STATE&connect_redirect=1#wechat_redirect");
    }

    @RequestMapping(value = "/app/weixinlogin")
    public ModelAndView weixinlogin(HttpServletResponse response) throws Exception
    {
        ModelAndView mv = new ModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String path = this.getRequest().getContextPath();
        String url = this.getRequest().getScheme() + "://" + this.getRequest().getServerName() + ":" + this.getRequest().getServerPort() + path + "/index";
//		String url = pd.getString("url");
        PageData info = GetInfo.userInfo(pd, this.getRequest());
        log.info("获取微信用户信息 ---> " + info);
        // String unionid = info.getString("unionid");
        String openid = info.getString("openid");
        pd.put("openid", openid);
        PageData shopUser = shopUserService.findById(pd);
        if (shopUser == null)
        { // 用户不存在
            pd.put("addtime", DatetimeUtil.getDatetime());
            pd.put("user_id", this.get32UUID());
            pd.put("head_img", info.get("headimgurl"));
            pd.put("username", info.get("nickname"));
            pd.put("openid", info.get("openid"));
            CookieUtil.cookieadd(response, "openid", openid);
            CookieUtil.cookieadd(response, "weixin", "weixin");
            shopUserService.save(pd);
            this.getRequest().getSession().setAttribute("shopUser", shopUser);
            this.getRequest().getSession().setAttribute("cart_count", 0);
        } else
        {
            this.getRequest().getSession().setAttribute("shopUser", shopUser);
            pd.put("user_id", shopUser.getString("user_id"));
            this.getRequest().getSession().setAttribute("cart_count", cartService.count(pd));
        }
//		String path = this.getRequest().getContextPath();
//		url = this.getRequest().getScheme()+"://"+this.getRequest().getServerName()+":"+this.getRequest().getServerPort()+path+"/" +url;
        mv.setViewName("redirect:" + url);
        return mv;
    }

    @ResponseBody
    @RequestMapping(value = "/app/getUser", produces = "application/json;charset=UTF-8")
    public String appgetUser() throws Exception
    {
        PageData pd = new PageData();
        pd = this.getPageData();
        PageData shopUser = shopUserService.findById(pd);
        this.getRequest().getSession().setAttribute("shopUser", shopUser);
        return gson.toJson(shopUser);
    }

    @RequestMapping(value = "/getUser", produces = "application/json;charset=UTF-8")
    public ModelAndView getUser() throws Exception
    {
        ModelAndView mv = new ModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        PageData user = StringUtil.shopUser(this.getRequest().getSession());
        pd.put("user_id", user.getString("user_id"));
        PageData shopUser = shopUserService.findById(pd);
        this.getRequest().getSession().setAttribute("shopUser", shopUser);
        mv.setViewName("center/index");
        return mv;
    }

    @ResponseBody
    @RequestMapping(value = "/app/getcode", produces = "application/json;charset=UTF-8")
    public String getcode()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        int result = 0;
        String message = "";
        try
        {
            PageData pd = new PageData();
            pd = this.getPageData();
            String phone = pd.getString("phone");
            ResultBean resultBean = phoneCaptchaService.sendYzmMsg(phone);
            if (resultBean.getIsSuccess())
            {
                message = "发送成功！";
                map.put("id", resultBean.getDataMap().get("id"));
            } else
            {
                result = -1;
                message = "发送失败！";
            }
        } catch (Exception e)
        {
            result = -1;
            message = "服务器异常！";
        }
        map.put("result", result);
        map.put("message", message);
        return gson.toJson(map);
    }

    /**
     * 验证2次输入的密码
     *
     * @return
     */
    public boolean checkpassword()
    {
        PageData pd = new PageData();
        pd = this.getPageData();
        if (pd.getString("password").equals(pd.getString("password1")))
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 修改
     *
     * @param
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/app/forget", produces = "application/json;charset=UTF-8")
    public String edit() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        int result = 0;
        String message = "";
        PageData pd = new PageData();
        pd = this.getPageData();
        String phone = pd.getString("phone");
        String code = pd.getString("code");
        Integer captchaId = Integer.valueOf(pd.getString("captchaId"));
        PageData isUser = shopUserService.findByPhone(pd);
        if (isUser == null)
        {
            if (checkpassword())
            { // 验证两次输入的密码
                if (phoneCaptchaService.checkYzm(captchaId, code, phone))
                {// 验证短信
                    pd.put("password", MD5.md5(pd.getString("password")));
                    shopUserService.edit(pd);
                    result = 1;
                    message = "修改成功！";

                } else
                {
                    result = 0;
                    message = "验证码错误！";
                }
            } else
            {
                result = 0;
                message = "两次密码不一致！";
            }
        } else
        {
            result = 0;
            message = "手机号已存在，请重新输入！";
        }
        map.put("result", result);
        map.put("message", message);
        return gson.toJson(map);
    }

    @ResponseBody
    @RequestMapping(value = "/user/upload", produces = "application/json;charset=UTF-8")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) throws Exception
    {
        PageData pd = new PageData();
        Map<String, Object> map = new HashMap<String, Object>();
        String realpath = request.getSession().getServletContext().getRealPath("");
        String path = "";
        if (realpath.contains("\\"))
        {
            path = realpath.substring(0, realpath.lastIndexOf("\\")) + "/mallupload"; // windows
        } else
        {
            path = realpath.substring(0, realpath.lastIndexOf("/")) + "/mallupload"; // linux
        }

        String fileName = new Date().getTime() + ".png";

        File targetFile = new File(path, fileName);
        if (!targetFile.exists())
        {
            targetFile.mkdirs();
        }
        // 保存
        file.transferTo(targetFile);
        String url = "/mallupload/" + fileName;
        PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
        pd.put("user_id", shopUser.getString("user_id"));
        pd.put("head_img", url);
        shopUserService.edit(pd);
        shopUser.put("head_img", url);
        map.put("result", 1);
        map.put("message", "提交成功！");
        map.put("img", url);
        return gson.toJson(map);
    }

    /**
     * 更新用户信息
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/user/update", produces = "application/json;charset=UTF-8")
    public String update() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        PageData pd = new PageData();
        pd = this.getPageData();
        PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
        pd.put("user_id", shopUser.getString("user_id"));
        shopUserService.edit(pd);
        PageData user = shopUserService.findById(pd);
        this.getRequest().getSession().setAttribute("shopUser", user);
        map.put("shopUser", user);
        map.put("result", 1);
        map.put("message", "提交成功！");
        return gson.toJson(map);
    }

    /**
     * 更新用户手机
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/user/update_phone", produces = "application/json;charset=UTF-8")
    public String update_phone() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        PageData pd = new PageData();
        pd = this.getPageData();
        int result = 0;
        String message = "";
        String phone = pd.getString("phone");
        String code = pd.getString("code");
        Integer captchaId = Integer.valueOf(pd.getString("captchaId"));
        if (phoneCaptchaService.checkYzm(captchaId, code, phone))
        {// 验证短信
            PageData isUser = shopUserService.findByPhone(pd);
            if (isUser == null)
            { // 用户存在

                PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
                pd.put("user_id", shopUser.getString("user_id"));
                shopUserService.update_phone(pd);
                PageData user = shopUserService.findById(pd);
                this.getRequest().getSession().setAttribute("shopUser", user);
                result = 1;
                message = "提交成功！";
            } else
            {
                message = "手机号已存在！";
            }
        } else
        {
            message = "验证码错误！";
        }
        map.put("result", result);
        map.put("message", message);
        return gson.toJson(map);
    }

    /**
     * 更新用户信息
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/app/password", produces = "application/json;charset=UTF-8")
    public String getpassword() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        int result = 0;
        String message = "";
        PageData pd = new PageData();
        pd = this.getPageData();
        String phone = pd.getString("phone");
        String code = pd.getString("code");
        String password = MD5.md5(pd.getString("password"));
        Integer captchaId = Integer.valueOf(pd.getString("captchaId"));
        if (phoneCaptchaService.checkYzm(captchaId, code, phone))
        {// 验证短信
            // PageData shopUser = (PageData)
            // this.getRequest().getSession().getAttribute("phonecode");
            pd.put("phone", phone);
            pd.put("password", password);
            shopUserService.setpassword(pd);
            result = 1;
            message = "提交成功！";
            PageData user = shopUserService.findByPhone(pd);
            this.getRequest().getSession().setAttribute("shopUser", user);
            map.put("shopUser", user);

        } else
        {
            result = 0;
            message = "验证码错误！";
        }
        map.put("result", result);
        map.put("message", message);
        return gson.toJson(map);
    }

    /**
     * 注销
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/remove", produces = "application/json;charset=UTF-8")
    public String remove()
    {
        String result = "1";
        try
        {
            this.getApp().removeAttribute("page");
        } catch (Exception e)
        {
            result = "2";
        }
        return result;
    }
}
