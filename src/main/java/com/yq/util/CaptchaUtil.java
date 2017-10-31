package com.yq.util;

import java.util.Random;

/**
 * 验证码工具类
 *
 * @author Liyong He
 * @version 1.0
 */
public class CaptchaUtil
{
    /**
     * 生成随机的验证码，可指定长度
     *
     * @param length 验证码长度
     * @return 全部由数字组成的随机验证码
     */
    public static String genCaptcha( int length )
    {
        Random random = new Random( System.currentTimeMillis() );
        if ( length <=0 )
        {
            throw new IllegalArgumentException( "验证码长度不能小于等于0" );
        }
        String captchaStr = "";
        for ( int i = 0; i < length; i++ )
        {
            captchaStr += random.nextInt( 10 );
        }
        return captchaStr;
    }

    public static String genSMSText(String captcha)
    {
        //return genSMSText( captcha, "会互动" );
        return genSMSText( captcha, "企酷金服" );
    }

    public static String genSMSText(String captcha, String sign )
    {
        return "【" + sign + "】您的验证码是" + captcha;
    }
}
