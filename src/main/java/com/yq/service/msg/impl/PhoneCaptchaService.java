package com.yq.service.msg.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.yq.entity.ResultBean;
import com.yq.service.msg.PhoneCaptchaManage;
import com.yq.service.phone.dao.PhoneCaptchaMapper;
import com.yq.util.CaptchaUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>名称</p>
 * <p/>
 * <p>wikiURL</p>
 *
 * @author zb.jiang
 * @version 1.0
 * @Date 2017/10/23
 */
@Service("phoneCaptchaService")
public class PhoneCaptchaService implements PhoneCaptchaManage {

    private static Logger logger = Logger.getLogger(PhoneCaptchaService.class);

    //产品名称:云通信短信API产品,开发者无需替换
    static final String product = "Dysmsapi";
    //产品域名,开发者无需替换
    static final String domain = "dysmsapi.aliyuncs.com";

    private static String defaultSign = "摩礼商城";

    @Value("#{runtimeProperties['aliyun.AccessKeyId']}")
    private String accessKeyId;

    @Value("#{runtimeProperties['aliyun.AccessKeySecret']}")
    private String accessKeySecret;

    @Autowired
    private PhoneCaptchaMapper phoneCaptchaMapper;

    @Override
    public ResultBean sendYzmMsg(String phone)
    {
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        ResultBean resultBean = new ResultBean();
        boolean sendFlag = true;

        //1分钟内不能重复发送
        Map<String, Object> captchaMap = phoneCaptchaMapper.qryPhoneNewestCaptCha(phone);
        if (captchaMap != null && !captchaMap.isEmpty())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -1);
            Date createTime = (Date) captchaMap.get("createTime");
            if (calendar.getTime().before(createTime))
            {
                sendFlag = false;
                resultBean.setIsSuccess(Boolean.FALSE);
                resultBean.setReturnMsg("1分钟内不可重复发送");
                resultBean.setCode("9998");

                logger.debug("手机号码[" + phone + "],1分钟内不可重复发送");
            }
        }

        // 1 生成验证码
        String captcha = CaptchaUtil.genCaptcha(4);

        if (sendFlag)
        {
            // 2 将验证码记录到表中
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("phone", phone);
            paramsMap.put("confirmCode", captcha);
            // 2.1 将验证码有效期设置为10分钟
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 10);
            paramsMap.put("expireTime", calendar.getTime());
            phoneCaptchaMapper.insert(paramsMap);

            Integer id = (Integer) paramsMap.get("id");
            if (id == null)
            {
                sendFlag = false;
                resultBean.setIsSuccess(Boolean.FALSE);
                resultBean.setReturnMsg("验证码保存数据库失败");
                resultBean.setCode("9997");

                logger.debug("手机号码[" + phone + "],验证码保存数据库失败");
            } else
            {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("id", id);
                resultBean.setDataMap(dataMap);
            }
        }

        if (sendFlag)
        {
            logger.debug("准备向[" + phone + "]发送短信验证码[" + captcha + "]");
            try
            {
                //初始化acsClient,暂不支持region化
                IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
                DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
                IAcsClient acsClient = new DefaultAcsClient(profile);

                //组装请求对象-具体描述见控制台-文档部分内容
                SendSmsRequest request = new SendSmsRequest();
                //必填:待发送手机号
                request.setPhoneNumbers(phone);
                //必填:短信签名-可在短信控制台中找到
                request.setSignName(defaultSign);
                //必填:短信模板-可在短信控制台中找到
                request.setTemplateCode("SMS_105520041");
                //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
                request.setTemplateParam("{\"code\":\"" + captcha + "\"}");

                //hint 此处可能会抛出异常，注意catch
                SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
                logger.debug("手机号码[" + phone + "],发送返回结果:" + sendSmsResponse);

                if ("OK".equals(sendSmsResponse.getCode()))
                {
                    resultBean.setIsSuccess(Boolean.TRUE);
                    resultBean.setReturnMsg(sendSmsResponse.getMessage());
                    resultBean.setCode(sendSmsResponse.getCode());
                } else
                {
                    resultBean.setIsSuccess(Boolean.FALSE);
                    resultBean.setReturnMsg(sendSmsResponse.getMessage());
                    resultBean.setCode(sendSmsResponse.getCode());
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                resultBean.setIsSuccess(Boolean.FALSE);
                resultBean.setReturnMsg("发送短信异常");
                resultBean.setCode("9999");

                logger.debug("手机号码[" + phone + "],发送短信异常");
            }
        }

        return resultBean;
    }

    /**
     * 校验验证码
     * @param id
     * @param code
     * @return
     */
    @Override
    public boolean checkYzm(Integer id, String code, String phone)
    {
        Map<String,Object> phoneMap = phoneCaptchaMapper.getPhoneCaptchaById(id);
        if(phoneMap == null || phoneMap.isEmpty())
        {
            return false;
        }

        if(!phone.equals(phoneMap.get("phone")) || !code.equals(phoneMap.get("confirmCode")))
        {
            return false;
        }

        // 确认是否超时
        Date now = new Date();
        Date expireTime = (Date) phoneMap.get("expireTime");
        if ( now.before( expireTime ) )
        {
            phoneCaptchaMapper.expirePhoneCode(id);
            return true;
        }
        else
        {
            return false;
        }
    }
}
