package com.theplatform.dfh.cp.modules.jsonhelper;

public class SampleObject
{
    private String id;
    private SampleObject subObject;

    public SampleObject()
    {

    }

    public String getId()
    {
        return id;
    }

    public SampleObject setId(String id)
    {
        this.id = id;
        return this;
    }

    public SampleObject getSubObject()
    {
        return subObject;
    }

    public SampleObject setSubObject(SampleObject subObject)
    {
        this.subObject = subObject;
        return this;
    }
}
