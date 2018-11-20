package com.theplatform.dfh.cp.resourcepool.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Insight
{
    private String id = UUID.randomUUID().toString();
    private String resourcePoolId;
    private List<String> tags = new ArrayList<>();
    private String queueName;
    private int queueSize;
    private SchedulingAlgorithm schedulingAlgorithm;

    public String getId()
    {
        return id;
    }

    public Insight setId(String id)
    {
        this.id = id;
        return this;
    }

    public String getResourcePoolId()
    {
        return resourcePoolId;
    }

    public Insight setResourcePoolId(String resourcePoolId)
    {
        this.resourcePoolId = resourcePoolId;
        return this;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public Insight setTags(List<String> tags)
    {
        this.tags = tags;
        return this;
    }
    public Insight addTag(String tag)
    {
        if(tags == null)
            tags = new ArrayList<>();
        tags.add(tag);
        return this;
    }
    public String getQueueName()
    {
        return queueName;
    }

    public Insight setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }

    public int getQueueSize()
    {
        return queueSize;
    }

    public Insight setQueueSize(int queueSize)
    {
        this.queueSize = queueSize;
        return this;
    }

    public SchedulingAlgorithm getSchedulingAlgorithm()
    {
        return schedulingAlgorithm;
    }

    public Insight setSchedulingAlgorithm(
            SchedulingAlgorithm schedulingAlgorithm)
    {
        this.schedulingAlgorithm = schedulingAlgorithm;
        return this;
    }
    public Insight setSchedulingAlgorithm(
            String schedulingAlgorithm)
    {
        if(schedulingAlgorithm == null) return this;

        this.schedulingAlgorithm = SchedulingAlgorithm.valueOf(schedulingAlgorithm);

        return this;
    }

}
