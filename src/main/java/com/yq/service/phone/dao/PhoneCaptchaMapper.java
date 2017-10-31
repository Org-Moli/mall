package com.yq.service.phone.dao;

import org.apache.ibatis.annotations.*;

import java.util.Map;

/**
 * <p>名称</p>
 * <p/>
 * <p>wikiURL</p>
 *
 * @author zb.jiang
 * @version 1.0
 * @Date 2017/10/20
 */
public interface PhoneCaptchaMapper {

    @Select({
            "select * from phone_captcha where phone = #{phone} order by createTime desc limit 1"
    })
    Map<String, Object> qryPhoneNewestCaptCha(@Param("phone") String phone);


    @Insert({
            "insert into phone_captcha(phone,confirmCode,createTime,expireTime)",
            "values",
            "(#{phone},#{confirmCode},now(),#{expireTime})"
    })
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", keyColumn = "id", before = false, resultType = Integer.class)
    int insert(Map<String, Object> paramsMap);


    @Select({
            "select * from phone_captcha where id = #{id}"
    })
    Map<String, Object> getPhoneCaptchaById(@Param("id") Integer id);

    @Update({
            "update phone_captcha set expireTime = now() where id = #{id}"
    })
    void expirePhoneCode(@Param("id") Integer id);
}
