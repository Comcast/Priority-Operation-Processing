package com.comcast.pop.scheduling.queue;

import com.comcast.pop.object.api.IdentifiedObject;

import java.util.Date;
import java.util.List;

public class InsightScheduleInfo implements IdentifiedObject
{
    private String id;
    private Date lastExecuted;
    private String lastMessage;
    private List<String> pendingCustomerIds;
    private String customerId;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getCustomerId()
    {
        return customerId;
    }

    @Override
    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

    public Date getLastExecuted()
    {
        return lastExecuted;
    }

    public void setLastExecuted(Date lastExecuted)
    {
        this.lastExecuted = lastExecuted;
    }

    public List<String> getPendingCustomerIds()
    {
        return pendingCustomerIds;
    }

    public void setPendingCustomerIds(List<String> pendingCustomerIds)
    {
        this.pendingCustomerIds = pendingCustomerIds;
    }

    public String getLastMessage()
    {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage)
    {
        this.lastMessage = lastMessage;
    }

    public static String generateId(String resourcePoolId, String insightId)
    {
        return resourcePoolId + "-" + insightId;
    }
}
