package com.theplatform.dfh.endpoint.api.query.progress;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 * ByLinkId field equality query.
 */
public class ByLinkId extends Query<String>
{
    private static final DataObjectField field = new DataObjectField("linkId");
    public ByLinkId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByLinkId query requires a non-empty value.");
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

