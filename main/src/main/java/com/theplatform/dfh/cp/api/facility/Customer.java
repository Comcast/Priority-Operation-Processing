package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.EndpointDataObject;

public class Customer extends EndpointDataObject
{
    private String billingCode;
    private String title;
    private String resourcePoolId;

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

    public String getResourcePoolId()
    {
        return resourcePoolId;
    }

    public void setResourcePoolId(String resourcePoolId)
    {
        this.resourcePoolId = resourcePoolId;
    }
}
