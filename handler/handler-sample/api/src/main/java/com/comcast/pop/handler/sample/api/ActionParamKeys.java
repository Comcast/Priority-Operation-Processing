package com.comcast.pop.handler.sample.api;

import com.comcast.pop.api.params.ParamKey;

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
