package com.comcast.pop.scheduling.monitor.aws;

public class ResourcePoolMonitorRequest
{
    private String resourcePoolId;
    private String stageId;

    public ResourcePoolMonitorRequest()
    {
    }

    public String getResourcePoolId()
    {
        return resourcePoolId;
    }

    public ResourcePoolMonitorRequest setResourcePoolId(String resourcePoolId)
    {
        this.resourcePoolId = resourcePoolId;
        return this;
    }

    public String getStageId()
    {
        return stageId;
    }

    public ResourcePoolMonitorRequest setStageId(String stageId)
    {
        this.stageId = stageId;
        return this;
    }
}
