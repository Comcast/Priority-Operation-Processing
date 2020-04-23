package com.comcast.pop.api.facility;

import com.comcast.pop.api.AllowedCustomerEndpointDataObject;
import com.comcast.pop.api.GlobalEndpointDataObject;

import java.util.*;

public class Insight extends AllowedCustomerEndpointDataObject implements GlobalEndpointDataObject
{
    private String resourcePoolId;
    private String queueName;
    private Integer queueSize;
    private SchedulingAlgorithm schedulingAlgorithm;
    private Map<String, Set<String>> mappers;
    private String title;
    private Boolean isGlobal;

    public String getResourcePoolId()
    {
        return resourcePoolId;
    }

    public void setResourcePoolId(String resourcePoolId)
    {
        this.resourcePoolId = resourcePoolId;
    }

    public Map<String, Set<String>> getMappers()
    {
        return mappers;
    }

    public void addMapper(InsightMapper mapper)
    {
        if(mapper == null) return;

        if(mappers == null)
            mappers = new TreeMap<>();
        mappers.put(mapper.getName(), mapper.getMatchValues());
    }

    public void setMappers(Map<String, Set<String>> mappers)
    {
        this.mappers = mappers;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }

    public Integer getQueueSize()
    {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize)
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

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Boolean isGlobal()
    {
        return isGlobal;
    }
    public Boolean getIsGlobal()
    {
        return isGlobal;
    }
    public void setIsGlobal(Boolean global)
    {
        isGlobal = global;
    }
}
