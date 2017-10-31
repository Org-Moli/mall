package com.yq.entity;

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
public class ResultBean {

    private boolean isSuccess;

    private String code;

    private String returnMsg;

    private Map<String,Object> dataMap;

    public boolean getIsSuccess()
    {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess)
    {
        this.isSuccess = isSuccess;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getReturnMsg()
    {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg)
    {
        this.returnMsg = returnMsg;
    }

    public Map<String, Object> getDataMap()
    {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap)
    {
        this.dataMap = dataMap;
    }
}
