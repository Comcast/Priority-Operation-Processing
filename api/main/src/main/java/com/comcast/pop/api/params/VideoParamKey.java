package com.comcast.pop.api.params;

public enum VideoParamKey implements ParamKey
{
    bFrames,
    bitrate,
    bufferSize,
    cabac,
    chromaSubsampling,
    codec,
    colorSpace,
    displayAspectRatio,
    duration,
    framerate,
    gopSize,
    height,
    id,
    level,
    profile,
    scanOrder,
    scanType,
    streamOrder,
    streamSize,
    width,
    ;

    public String getKey()
    {
        return this.name();
    }
}
