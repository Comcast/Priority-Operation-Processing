package com.comast.pop.handler.base.field.retriever.api;

import org.apache.commons.lang3.math.NumberUtils;

public abstract class FieldRetriever
{
    public abstract String getField(String field);
    public abstract String getField(String field, String defaultValue);
    public abstract boolean isFieldSet(String field);

    public Long getLong(String field, Long defaultValue)
    {
        return NumberUtils.toLong(getField(field), defaultValue);
    }
    public Integer getInt(String field, Integer defaultValue)
    {
        return NumberUtils.toInt(getField(field), defaultValue);
    }
    public Boolean getBoolean(String field, Boolean defaultValue)
    {
        String value = getField(field);
        if(value == null)
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(getField(field));
    }
    public Double getDouble(String field, Double defaultValue)
    {
        return NumberUtils.toDouble(getField(field), defaultValue);
    }

    public String getField(NamedField field)
    {
        return getField(field.getFieldName());
    }
    public String getField(NamedField field, String defaultValue)
    {
        return getField(field.getFieldName(), defaultValue);
    }
    public boolean isFieldSet(NamedField field)
    {
        return isFieldSet(field.getFieldName());
    }
    public Long getLong(NamedField field, Long defaultValue)
    {
        return getLong(field.getFieldName(), defaultValue);
    }
    public Integer getInt(NamedField field, Integer defaultValue)
    {
        return getInt(field.getFieldName(), defaultValue);
    }
    public Boolean getBoolean(NamedField field, Boolean defaultValue)
    {
        return getBoolean(field.getFieldName(), defaultValue);
    }
    public Double getDouble(NamedField field, Double defaultValue)
    {
        return getDouble(field.getFieldName(), defaultValue);
    }

}
