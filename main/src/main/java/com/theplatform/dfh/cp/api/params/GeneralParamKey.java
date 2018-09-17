package com.theplatform.dfh.cp.api.params;

public enum GeneralParamKey implements ParamKey
{
    progressId;

    public String getKey()
    {
        return this.name();
    }
}
