package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.object.api.IdentifiedObject;

public class EndpointDataObject implements IdentifiedObject
{
    private String id;
    private String cid;
    private String customerId;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
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

    public void setCid(String cid)
    {
        this.cid = cid;
    }

    public String getCid()
    {
        return cid;
    }
}
