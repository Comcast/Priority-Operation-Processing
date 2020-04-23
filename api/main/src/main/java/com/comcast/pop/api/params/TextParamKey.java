package com.comcast.pop.api.params;

public enum TextParamKey implements ParamKey
{
    languageCode, intent;

    public String getKey()
    {
        return this.name();
    }
}
