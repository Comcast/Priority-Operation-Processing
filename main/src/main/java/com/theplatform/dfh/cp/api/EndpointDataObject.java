package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.Date;

public class EndpointDataObject implements IdentifiedObject
{
    private String id;
    private String cid;
    private String customerId;
    private Date updatedTime;
    private Date addedTime;

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

    public Date getUpdatedTime()
    {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime)
    {
        this.updatedTime = updatedTime;
    }

    public Date getAddedTime()
    {
        return addedTime;
    }

    public void setAddedTime(Date addedTime)
    {
        this.addedTime = addedTime;
    }
}
