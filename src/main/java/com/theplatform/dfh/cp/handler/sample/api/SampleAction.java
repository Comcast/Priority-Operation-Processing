package com.theplatform.dfh.cp.handler.sample.api;

import com.theplatform.dfh.cp.api.params.ParamsMap;

public class SampleAction
{
    private String action;
    private ParamsMap paramsMap;

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public ParamsMap getParamsMap()
    {
        return paramsMap;
    }

    public void setParamsMap(ParamsMap paramsMap)
    {
        this.paramsMap = paramsMap;
    }

}
