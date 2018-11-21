package com.theplatform.dfh.persistence.api;

public class DataTypeField
{
   private String fieldName;

    public DataTypeField(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String name()
    {
        return fieldName;
    }
}
