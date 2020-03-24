package com.theplatform.dfh.cp.api.params;

public enum TextParamKey implements ParamKey
{
    languageCode, intent;

    public String getKey()
    {
        return this.name();
    }
}
