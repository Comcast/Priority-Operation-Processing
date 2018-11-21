package com.theplatform.dfh.persistence.api.query;

import com.theplatform.dfh.persistence.api.DataTypeField;

import java.util.Collection;

public class Query<T>
{
    private DataTypeField field;
    private T value;
    private boolean isCollection;

    public Query()
    {
    }

    public Query(DataTypeField field, T value)
    {
        this.field = field;
        this.value = value;
        this.isCollection = value != null && value instanceof Collection;
    }

    public Query(DataTypeField field, T value, boolean isCollection)
    {
        this.field = field;
        this.value = value;
        this.isCollection = isCollection;
    }

    public DataTypeField getField()
    {
        return field;
    }

    public void setField(DataTypeField field)
    {
        this.field = field;
    }

    public void setValue(T value)
    {
        this.value = value;
        this.isCollection = value != null && value instanceof Collection;
    }

    public boolean isCollection()
    {
        return isCollection;
    }

    public void setCollection(boolean collection)
    {
        isCollection = collection;
    }

    public T getValue()
    {
        return value;
    }
    @Override
    public int hashCode()
    {
        int result = getField() != null
                ? getField().hashCode()
                : 0;
        result = 31 * result + (getValue() != null
                ? getValue().hashCode()
                : 0);
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Query that = (Query) o;
        if (getField() != null
                ? !getField().equals(that.getField())
                : that.getField() != null)
            return false;
        return getValue() != null
                ? getValue().equals(that.getValue())
                : that.getValue() == null;
    }

    @Override
    public String toString()
    {
        return "Query{" +
                "field=" + field +
                ", value=" + value +
                '}';
    }
}
