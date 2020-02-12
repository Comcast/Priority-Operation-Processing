package com.theplatform.dfh.cp.api.params;

public enum GeneralParamKey implements ParamKey
{
    progressId,
    execProgressId,
    agendaId,
    dependsOn,
    cid,
    customerId,
    externalId,
    maximumAttempts,
    mpxTaskSourceFiles,
    mpxTaskRemovedFiles,
    mpxTaskResultFile,
    doNotRun;

    public String getKey()
    {
        return this.name();
    }
}
