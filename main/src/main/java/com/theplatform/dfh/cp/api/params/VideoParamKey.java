package com.theplatform.dfh.cp.api.params;

public enum VideoParamKey implements ParamKey
{
    width, height, bitrate, codec, profile, level, bufferSize, cabac, gopSize;

    public String getKey()
    {
        return this.name();
    }
}
