package com.comcast.pop.api.params;

public enum GeneralParamKey implements ParamKey
{
    progressId,
    execProgressId,
    agendaId,
    dependsOn,
    generatedOperationParent,
    cid,
    customerId,
    externalId,
    maximumAttempts,
    doNotRun;

    public String getKey()
    {
        return this.name();
    }
}
