package com.theplatform.dfh.cp.handler.sample.api;

import com.theplatform.dfh.cp.api.params.ParamKey;

public enum ActionParamKeys implements ParamKey
{
    sleepMilliseconds,
    logMessage,
    exceptionMessage;

    @Override
    public String getKey()
    {
        return name();
    }
}
