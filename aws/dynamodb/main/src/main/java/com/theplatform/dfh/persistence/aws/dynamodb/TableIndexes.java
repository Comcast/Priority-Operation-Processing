package com.theplatform.dfh.persistence.aws.dynamodb;

import java.util.HashMap;

public class TableIndexes
{
    private HashMap<String, String> fieldToIndexMap = new HashMap<>();
    private String primaryKey = "id";

    public TableIndexes withPrimaryKey(String fieldName)
    {
        this.primaryKey = fieldName;
        return this;
    }
    public TableIndexes withIndex(String indexName, String fieldName)
    {
        this.fieldToIndexMap.put(fieldName, indexName);
        return this;
    }

    public boolean isPrimary(String field)
    {
       return field != null && primaryKey != null && field.equals(primaryKey);
    }
    public String getIndex(String field)
    {
        if(field == null) return null;
        return fieldToIndexMap.get(field);
    }
}
