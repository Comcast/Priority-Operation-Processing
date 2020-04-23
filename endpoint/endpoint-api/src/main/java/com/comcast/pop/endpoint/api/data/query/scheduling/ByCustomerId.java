package com.comcast.pop.endpoint.api.data.query.scheduling;

import com.comcast.pop.persistence.api.field.DataObjectField;
import com.comcast.pop.persistence.api.query.Query;

/**
 * ByCustomerId field equality query.
 */
public class ByCustomerId extends Query<String>
{
    private static final String fieldName = "customerId";
    public ByCustomerId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByCustomerId query requires a non-empty value.");
        }

        setField(new DataObjectField(fieldName));
        setValue(value);
        setCollection(false);
    }

    public static String fieldName()
    {
        return fieldName;
    }
}
