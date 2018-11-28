package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Insight implements IdentifiedObject
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

    public void setId(String id)
    {
        this.id = id;
    }

    public String getResourcePoolId()
    {
        return resourcePoolId;
    }

    public void setResourcePoolId(String resourcePoolId)
    {
        this.resourcePoolId = resourcePoolId;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }
    public void addTag(String tag)
    {
        if(tags == null)
            tags = new ArrayList<>();
        tags.add(tag);
    }
    public String getQueueName()
    {
        return queueName;
    }

    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }

    public int getQueueSize()
    {
        return queueSize;
    }

    public void setQueueSize(int queueSize)
    {
        this.queueSize = queueSize;
    }

    public SchedulingAlgorithm getSchedulingAlgorithm()
    {
        return schedulingAlgorithm;
    }

    public void setSchedulingAlgorithm(
            SchedulingAlgorithm schedulingAlgorithm)
    {
        this.schedulingAlgorithm = schedulingAlgorithm;
    }
    public void setSchedulingAlgorithm(
            String schedulingAlgorithm)
    {
        if(schedulingAlgorithm == null) return;

        this.schedulingAlgorithm = SchedulingAlgorithm.valueOf(schedulingAlgorithm);
    }

}
