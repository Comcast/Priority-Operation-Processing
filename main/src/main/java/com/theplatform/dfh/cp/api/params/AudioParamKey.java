package com.theplatform.dfh.cp.api.params;


public enum AudioParamKey implements ParamKey
{
    bitrate,
    channels,
    codec,
    duration,
    format,
    id,
    language,
    profile,
    samplingRate,
    sampleSize,
    streamSize,
    ;

    public String getKey()
    {
        return this.name();
    }
}
