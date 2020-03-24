package com.cts.fission.scheduling.master.aws;

public class SchedulerRequest
{
    private String stageId;

    public SchedulerRequest()
    {
    }

    public String getStageId()
    {
        return stageId;
    }

    public SchedulerRequest setStageId(String stageId)
    {
        this.stageId = stageId;
        return this;
    }
}
