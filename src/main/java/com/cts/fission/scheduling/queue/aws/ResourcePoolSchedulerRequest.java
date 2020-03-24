package com.cts.fission.scheduling.queue.aws;

public class ResourcePoolSchedulerRequest
{
    private String resourcePoolId;
    private String stageId;

    public ResourcePoolSchedulerRequest()
    {
    }

    public String getResourcePoolId()
    {
        return resourcePoolId;
    }

    public ResourcePoolSchedulerRequest setResourcePoolId(String resourcePoolId)
    {
        this.resourcePoolId = resourcePoolId;
        return this;
    }

    public String getStageId()
    {
        return stageId;
    }

    public ResourcePoolSchedulerRequest setStageId(String stageId)
    {
        this.stageId = stageId;
        return this;
    }
}
