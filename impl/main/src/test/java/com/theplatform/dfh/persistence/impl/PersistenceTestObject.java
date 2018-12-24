package com.theplatform.dfh.persistence.impl;

import com.theplatform.dfh.object.api.IdentifiedObject;

public class PersistenceTestObject implements IdentifiedObject
{
    private String id;
    private String val;

    public PersistenceTestObject()
    {

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
