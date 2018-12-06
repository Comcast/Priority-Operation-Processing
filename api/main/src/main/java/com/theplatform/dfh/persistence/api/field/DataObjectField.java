package com.theplatform.dfh.persistence.api.field;

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
}
