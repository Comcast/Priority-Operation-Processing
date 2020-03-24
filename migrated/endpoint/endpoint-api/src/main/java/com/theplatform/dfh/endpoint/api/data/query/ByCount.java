package com.theplatform.dfh.endpoint.api.data.query;

import com.theplatform.dfh.persistence.api.field.CountField;
import com.theplatform.dfh.persistence.api.query.Query;

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

