package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class Customer implements IdentifiedObject
{
    private String id;
    private String billingCode;
    private String title;
    private List<String> availableResourcePoolIds = new ArrayList<>();

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

    public List<String> getAvailableResourcePoolIds()
    {
        return availableResourcePoolIds;
    }

    public void setAvailableResourcePoolIds(List<String> availableResourcePoolIds)
    {
        this.availableResourcePoolIds = availableResourcePoolIds;
    }

    public void addAvailableResourcePool(ResourcePool availableResourcePool)
    {
        if(this.availableResourcePoolIds == null)
            this.availableResourcePoolIds = new ArrayList<>();
        
        this.availableResourcePoolIds.add(availableResourcePool.getId());
    }
}
