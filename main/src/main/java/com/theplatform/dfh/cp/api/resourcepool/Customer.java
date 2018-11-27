package com.theplatform.dfh.cp.api.resourcepool;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class Customer implements IdentifiedObject
{
    private String id;
    private String billingCode;
    private String title;
    private List<String> availableInsightIds = new ArrayList<>();

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getBillingCode()
    {
        return billingCode;
    }

    public void setBillingCode(String billingCode)
    {
        this.billingCode = billingCode;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public List<String> getAvailableInsightIds()
    {
        return availableInsightIds;
    }

    public void setAvailableInsightIds(List<String> availableInsightIds)
    {
        this.availableInsightIds = availableInsightIds;
    }

    public void addAvailableInsight(Insight availableInsight)
    {
        if(this.availableInsightIds == null)
            this.availableInsightIds = new ArrayList<>();
        
        this.availableInsightIds.add(availableInsight.getId());
    }
}
