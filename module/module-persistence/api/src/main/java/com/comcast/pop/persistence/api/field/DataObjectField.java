package com.comcast.pop.persistence.api.field;

public class DataObjectField implements DataField
{
   private String fieldName;

    public DataObjectField(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String name()
    {
        return fieldName;
    }

    @Override
    public boolean isMatch(String fieldName)
    {
        return fieldName != null && fieldName.equalsIgnoreCase(this.fieldName);
    }
}
