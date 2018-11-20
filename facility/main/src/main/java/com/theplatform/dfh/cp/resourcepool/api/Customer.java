package com.theplatform.dfh.cp.resourcepool.api;

import java.util.ArrayList;
import java.util.List;

public class Customer
{
    private String id;
    private String billingCode;
    private String title;
    private List<String> availableInsightIds = new ArrayList<>();

    public String getId()
    {
        return id;
    }

    public Customer setId(String id)
    {
        this.id = id;
        return this;
    }

    public String getBillingCode()
    {
        return billingCode;
    }

    public Customer setBillingCode(String billingCode)
    {
        this.billingCode = billingCode;
        return this;
    }

    public String getTitle()
    {
        return title;
    }

    public Customer setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public List<String> getAvailableInsightIds()
    {
        return availableInsightIds;
    }

    public Customer setAvailableInsightIds(List<String> availableInsightIds)
    {
        this.availableInsightIds = availableInsightIds;
        return this;
    }

    public Customer addAvailableInsight(Insight availableInsight)
    {
        if(this.availableInsightIds == null)
            this.availableInsightIds = new ArrayList<>();
        
        this.availableInsightIds.add(availableInsight.getId());
        return this;
    }
}
