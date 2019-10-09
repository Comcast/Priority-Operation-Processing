package com.theplatform.dfh.cp.api;

import java.util.Set;
import java.util.TreeSet;

public class AllowedCustomerEndpointDataObject extends EndpointDataObject
{
    private boolean isGlobal = false;
    private Set<String> allowedCustomerIds;

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

}
