package com.comcast.pop.persistence.impl;

import com.comcast.pop.object.api.IdentifiedObject;

public class PersistenceTestObject implements IdentifiedObject
{
    private String id;
    private String val;
    private String customerId;

    public PersistenceTestObject()
    {

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
    public String getId()
    {
        return id;
    }

    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    public String getVal()
    {
        return val;
    }

    public void setVal(String val)
    {
        this.val = val;
    }
}
