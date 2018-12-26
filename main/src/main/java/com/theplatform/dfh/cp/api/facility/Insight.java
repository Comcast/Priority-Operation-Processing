package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.*;

public class Insight implements IdentifiedObject
{
    private String id;
    private String resourcePoolId;
    private String queueName;
    private int queueSize;
    private SchedulingAlgorithm schedulingAlgorithm;
    private Map<String, Set<String>> mappers;
    private Set<String> allowedCustomerIds;

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

    public boolean isGlobal()
    {
        return allowedCustomerIds == null || allowedCustomerIds.size() == 0;
    }

    public void addAllowedCustomer(String customerId)
    {
        if (customerId != null)
        {
            if(allowedCustomerIds == null)
                allowedCustomerIds = new TreeSet<>();
            this.allowedCustomerIds.add(customerId.toLowerCase());
        }
    }
    public void setAllowedCustomerIds(Set<String> customerIds)
    {
        if(customerIds != null)
        {
            for(String customerId : customerIds)
                addAllowedCustomer(customerId);
        }
    }

    public Set<String> getAllowedCustomerIds()
    {
        return allowedCustomerIds;
    }

    public boolean isVisible(final String customerId)
    {
        if(isGlobal()) return true;
        if(customerId == null) return false;

        return allowedCustomerIds.contains(customerId.toLowerCase());
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
