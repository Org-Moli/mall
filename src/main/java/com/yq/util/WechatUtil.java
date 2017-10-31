package com.yq.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;

/**
 * <p>名称</p>
 * <p/>
 * <p>wikiURL</p>
 *
 * @author zb.jiang
 * @version 1.0
 * @Date 2017/10/26
 */
public class WechatUtil {

    private static Logger logger = Logger.getLogger(WechatUtil.class);

    /**
     * JSP_TICKET过期时间
     */
    private static long JSP_TICKET_EXPIRE_TIME = 0;

    /**
     * JSP_TICKET线程所
     */
    private static final String JSP_TICKET_LOCK = "JSP_TICKET";

    /**
     * 缓存的JSP_TICKET
     */
    private static String JSP_TICKET;

    /**
     * 获得JSP_TICKET
     *
     * @param token  微信应用ID
     * @return JSP访问的TICKET
     */
    public static String getJspTicket(String token)
    {
        logger.info("------------------------->>>>token:" + token);
        logger.info("------------------------->>>>JSP_TICKET_EXPIRE_TIME:" + JSP_TICKET_EXPIRE_TIME);
        long now = System.currentTimeMillis();

        if (now > JSP_TICKET_EXPIRE_TIME)
        {
            synchronized (JSP_TICKET_LOCK)
            {
                HttpClient httpClient = new HttpClient();
                NameValuePair[] nameValuePairs = new NameValuePair[2];
                nameValuePairs[0] = new NameValuePair("access_token", token);
                nameValuePairs[1] = new NameValuePair("type", "jsapi");
                GetMethod method = new GetMethod("https://api.weixin.qq.com/cgi-bin/ticket/getticket");
                method.setQueryString(nameValuePairs);
                HttpMethodParams param = method.getParams();
                param.setContentCharset("UTF-8");
                try
                {
                    httpClient.executeMethod(method);
                    String returnMsg = method.getResponseBodyAsString();
                    JSONObject jsonObject = new JSONObject(returnMsg);
                    Integer returnCode = jsonObject.getInt("errcode");
                    logger.info("---------------getJspTicket.returnCode:" + returnCode);
                    if (returnCode != 0)
                    {
                        return null;
                    } else
                    {
                        JSP_TICKET = jsonObject.getString("ticket");
                        JSP_TICKET_EXPIRE_TIME = now + 7000 * 1000; // JSP_TICKET过期时间为7200s，本处处理为7000s
                        return JSP_TICKET;
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
        } else
        {
            return JSP_TICKET;
        }
    }
}
