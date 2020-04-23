package com.comcast.pop.persistence.api.field;

public class LimitField extends DataObjectField
{
    private static final String fieldName = "limit";
    private static final Integer defaultValue = 100;

    public LimitField()
    {
        super(fieldName);
    }

    public static String fieldName()
    {
        return fieldName;
    }
    public static Integer defaultValue()
    {
        return defaultValue;
    }
}
