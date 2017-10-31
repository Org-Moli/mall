package com.yq.service.msg;

import com.yq.entity.ResultBean;

/**
 * <p>名称</p>
 * <p/>
 * <p>wikiURL</p>
 *
 * @author zb.jiang
 * @version 1.0
 * @Date 2017/10/23
 */
public interface PhoneCaptchaManage {

    ResultBean sendYzmMsg(String phone);

    boolean checkYzm(Integer id, String code, String phone);

}
