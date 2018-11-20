package com.theplatform.dfh.cp.resourcepool.api;

import java.util.ArrayList;
import java.util.List;

public class Customer
{
    private String id;
    private String billingCode;
    private String title;
    private List<Insight> availableInsights = new ArrayList<>();

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

    public List<Insight> getAvailableInsights()
    {
        return availableInsights;
    }

    public Customer setAvailableInsights(List<Insight> availableInsights)
    {
        this.availableInsights = availableInsights;
        return this;
    }
    public Customer addAvailableInsights(Insight availableInsight)
    {
        if(this.availableInsights == null)
            this.availableInsights = new ArrayList<>();
        
        this.availableInsights.add(availableInsight);
        return this;
    }
}
