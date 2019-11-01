package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.AllowedCustomerEndpointDataObject;
import com.theplatform.dfh.cp.api.GlobalEndpointDataObject;

import java.util.*;

public class Insight extends AllowedCustomerEndpointDataObject implements GlobalEndpointDataObject
{
    private String resourcePoolId;
    private String queueName;
    private int queueSize;
    private SchedulingAlgorithm schedulingAlgorithm;
    private Map<String, Set<String>> mappers;
    private String title;
    private boolean isGlobal = false;

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

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }
    public boolean getIsGlobal()
    {
        return isGlobal;
    }
    public void setIsGlobal(boolean global)
    {
        isGlobal = global;
    }
}
