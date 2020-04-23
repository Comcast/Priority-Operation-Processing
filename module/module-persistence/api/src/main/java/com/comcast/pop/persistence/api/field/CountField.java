package com.comcast.pop.persistence.api.field;

public class CountField extends DataObjectField
{
    private static final String fieldName = "count";
    private static final Boolean defaultValue = true;

    public CountField()
    {
        super(fieldName);
    }

    public static String fieldName()
    {
        return fieldName;
    }
    public static Boolean defaultValue()
    {
        return defaultValue;
    }
}
