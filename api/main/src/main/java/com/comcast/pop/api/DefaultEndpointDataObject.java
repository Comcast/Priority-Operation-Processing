package com.comcast.pop.api;

import com.comcast.pop.object.api.IdentifiedObject;

import java.util.Date;

public class DefaultEndpointDataObject implements EndpointDataObject, IdentifiedObject
{
    private String id;
    private String cid;
    private String customerId;
    private String title;
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

    @Override
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
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
