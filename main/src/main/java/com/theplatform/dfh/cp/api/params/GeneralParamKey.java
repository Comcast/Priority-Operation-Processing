package com.theplatform.dfh.cp.api.params;

public enum GeneralParamKey implements ParamKey
{
    progressId, execProgressId, agendaId, dependsOn, cid, externalId;

    public String getKey()
    {
        return this.name();
    }
}
