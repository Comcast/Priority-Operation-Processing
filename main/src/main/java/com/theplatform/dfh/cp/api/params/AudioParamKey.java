package com.theplatform.dfh.cp.api.params;


public enum AudioParamKey implements ParamKey
{
    bitrate, language;

    public String getKey()
    {
        return this.name();
    }
}
