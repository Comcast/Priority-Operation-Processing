package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.Map;

public class TestTrackedObject implements IdentifiedObject
{
    private String id;
    private String valueString;
    private Map<String, String> stringAttributes;
    private String customerId;

    public TestTrackedObject()
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

    public TestTrackedObject(String id, String valueString)
    {
        this.id = id;
        this.valueString = valueString;
    }

    public void setStringAttributes(Map<String, String> stringAttributes)
    {
        this.stringAttributes = stringAttributes;
    }

    public void setValueString(String valueString)
    {
        this.valueString = valueString;
    }

    public String getValueString()
    {
        return valueString;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TestTrackedObject that = (TestTrackedObject) o;

        return valueString != null
                ? valueString.equals(that.valueString)
                : that.valueString == null;
    }

    @Override
    public int hashCode()
    {
        return valueString != null
                ? valueString.hashCode()
                : 0;
    }
}
