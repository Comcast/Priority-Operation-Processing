package com.theplatform.dfh.persistence.aws.dynamodb;

import java.util.Map;

public class TestTrackedObject
{
    private String valueString;
    private Map<String, String> stringAttributes;

    public TestTrackedObject()
    {

    }

    public TestTrackedObject(String valueString)
    {
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
