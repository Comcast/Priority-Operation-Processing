package com.comcast.pop.api.output;

import com.comcast.pop.api.params.ParamKey;

public enum OutputParamKey implements ParamKey
{
    protectionScheme,
    format;

    public String getKey()
    {
        return this.name();
    }
}
