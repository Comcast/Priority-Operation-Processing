package com.theplatform.dfh.cp.api.output;

import com.theplatform.dfh.cp.api.params.ParamKey;

public enum OutputParamKey implements ParamKey
{
    protectionScheme,
    format;

    public String getKey()
    {
        return this.name();
    }
}
