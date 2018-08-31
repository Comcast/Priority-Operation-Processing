package com.theplatform.dfh.cp.api.params;

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
    streamSize,
    width,
    ;

    public String getKey()
    {
        return this.name();
    }
}
