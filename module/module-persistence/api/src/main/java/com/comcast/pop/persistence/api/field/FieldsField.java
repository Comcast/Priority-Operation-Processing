package com.comcast.pop.persistence.api.field;

public class FieldsField extends DataObjectField
{
    private static final String fieldName = "fields";

    public FieldsField()
    {
        super(fieldName);
    }

    public static String fieldName()
    {
        return fieldName;
    }
}
