package com.theplatform.dfh.cp.api.params;

public enum FileParamKey implements ParamKey
{
    fileSize;

    public String getKey()
    {
        return this.name();
    }
}
