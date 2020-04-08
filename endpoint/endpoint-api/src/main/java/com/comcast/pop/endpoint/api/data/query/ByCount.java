package com.comcast.pop.endpoint.api.data.query;

import com.comcast.pop.persistence.api.field.CountField;
import com.comcast.pop.persistence.api.query.Query;

/**
 * ByCount only returns the count of entries and not the actual entries
 */
public class ByCount extends Query<Boolean>
{
    private static final CountField field = new CountField();
    public ByCount(Boolean value)
    {
        if(value == null)
        {
            value = true;
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

