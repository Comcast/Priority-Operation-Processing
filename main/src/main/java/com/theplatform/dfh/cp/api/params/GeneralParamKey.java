package com.theplatform.dfh.cp.api.params;

public enum GeneralParamKey implements ParamKey
{
    progressId, agendaId;

    public String getKey()
    {
        return this.name();
    }
}
