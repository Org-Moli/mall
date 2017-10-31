package com.yq.util;

import com.weixin.util.Accesstoken;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * <p>微信JS接口注册登记工具类</p>
 *
 * @author Liyong He
 * @version 1.0
 */
public class WechatJSRegUtils {
    private static final Logger logger = Logger.getLogger(WechatJSRegUtils.class);

    /**
     * 获得注册微信JS接口的登记信息
     *
     * @return 登记信息bean
     */
    public static RegInfoBean getRegInfoBean(HttpServletRequest httpRequest)
    {
        logger.info("--------------------开始获取微信信息---------------");
        StringBuffer requestURL = httpRequest.getRequestURL();
        String queryStr = httpRequest.getQueryString();
        if (queryStr != null)
        {
            requestURL.append("?").append(queryStr);
        }

        try
        {
            // 1 生成一个随机字符串
            String noncestr = getRandomString(14); // 目前处理为14位长度
            String timestamp = String.valueOf(System.currentTimeMillis());
            String token = Accesstoken.getaccesstoken();
            logger.info("------------------->>>token111:" + token);
            String ticket = WechatUtil.getJspTicket(token);
            logger.info("------------------->>>ticket:" + ticket);
            StringBuilder str2Sign = new StringBuilder();
            str2Sign.append("jsapi_ticket=")
                    .append(ticket)
                    .append("&noncestr=")
                    .append(noncestr)
                    .append("&timestamp=")
                    .append(timestamp)
                    .append("&url=")
                    .append(requestURL.toString())
            ;
            String sign = signStr(str2Sign.toString());

            ResourceBundle bundle = PropertyResourceBundle.getBundle("runtimes");
            String appId = bundle.getString("wechat.appid");

            RegInfoBean bean = new RegInfoBean();
            bean.setAppId(appId);
            bean.setNonceStr(noncestr);
            bean.setSignature(sign);
            bean.setTimestamp(timestamp);
            // 得到主机地址
            bean.setHost(getHostStr(httpRequest));
            return bean;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得http的主机字符串，包括http://hostname:port部分
     *
     * @param httpServletRequest 当前页面的httprequest请求
     * @return 主机字符串
     */
    public static String getHostStr(HttpServletRequest httpServletRequest)
    {
        StringBuffer fullURL = httpServletRequest.getRequestURL();
        String requestURI = httpServletRequest.getRequestURI();
        if (requestURI != null)
        {
            return fullURL.substring(0, fullURL.length() - requestURI.length());
        } else
        {
            return fullURL.toString();
        }
    }

    static String signStr(String str2Enc)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(str2Enc.getBytes());
            byte[] src = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder("");
            if (src == null || src.length <= 0)
            {
                return null;
            }
            for (byte aSrc : src)
            {
                int v = aSrc & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2)
                {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static String getRandomString(int length)
    { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 包含注册信心的JavaBean
     */
    public static class RegInfoBean {
        private String timestamp;

        private String nonceStr;

        private String appId;

        private String signature;

        private String host;

        public String getHost()
        {
            return host;
        }

        public void setHost(String host)
        {
            this.host = host;
        }

        public String getTimestamp()
        {
            return timestamp;
        }

        public void setTimestamp(String timestamp)
        {
            this.timestamp = timestamp;
        }

        public String getNonceStr()
        {
            return nonceStr;
        }

        public void setNonceStr(String nonceStr)
        {
            this.nonceStr = nonceStr;
        }

        public String getAppId()
        {
            return appId;
        }

        public void setAppId(String appId)
        {
            this.appId = appId;
        }

        public String getSignature()
        {
            return signature;
        }

        public void setSignature(String signature)
        {
            this.signature = signature;
        }
    }
}
