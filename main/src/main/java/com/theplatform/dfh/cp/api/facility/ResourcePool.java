package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class ResourcePool implements IdentifiedObject
{
    private String id;
    private String title;
    private String customerId;
    private List<String> insightIds = null;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

    public void setInsightIds(List<String> insightIds)
    {
        this.insightIds = insightIds;
    }

    public List<String> getInsightIds()
    {
        return insightIds;
    }

    public void addInsightId(String insightId)
    {
        if(insightIds == null)
            insightIds = new ArrayList<>();
        insightIds.add(insightId);
    }

}
