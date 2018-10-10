package com.theplatform.dfh.cp.api.params;

public enum GeneralParamKey implements ParamKey
{
    progressId, agendaId, transformRequestId;

    public String getKey()
    {
        return this.name();
    }
}
