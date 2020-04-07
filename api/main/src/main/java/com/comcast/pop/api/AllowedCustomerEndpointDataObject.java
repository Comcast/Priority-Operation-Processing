package com.comcast.pop.api;

import java.util.Set;
import java.util.TreeSet;

public class AllowedCustomerEndpointDataObject extends DefaultEndpointDataObject
{
    private Set<String> allowedCustomerIds;

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
