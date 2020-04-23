package com.comcast.pop.endpoint.api.data.query;

import com.comcast.pop.persistence.api.field.DataObjectField;
import com.comcast.pop.persistence.api.query.Query;

/**
 * ByTitle field equality query.
 */
public class ByTitle extends Query<String>
{
    private static final DataObjectField field = new DataObjectField("title");
    public ByTitle(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByTitle query requires a non-empty value.");
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

