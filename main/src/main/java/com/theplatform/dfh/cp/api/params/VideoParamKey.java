package com.theplatform.dfh.cp.api.params;

public enum VideoParamKey implements ParamKey
{
    width, height, bitrate, codec, profile, level;

    public String getKey()
    {
        return this.name();
    }
}
