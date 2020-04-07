package com.comcast.pop.api.facility;

import com.comcast.pop.api.DefaultEndpointDataObject;

public class Customer extends DefaultEndpointDataObject
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
