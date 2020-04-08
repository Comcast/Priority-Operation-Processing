package com.comcast.pop.endpoint.api.data.query;

import com.comcast.pop.persistence.api.field.IdField;
import com.comcast.pop.persistence.api.query.Query;

/**
 * ById field equality query.
 */
public class ById extends Query<String>
{
    private static final IdField field = new IdField();
    public ById(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ById query requires a non-empty value.");
        }

        setField(field);
        setValue(value);
        setCollection(false);
    }

    public static String fieldName()
    {
        return field.name();
    }
}

