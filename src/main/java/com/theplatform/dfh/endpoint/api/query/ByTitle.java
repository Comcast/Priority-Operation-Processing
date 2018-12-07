package com.theplatform.dfh.endpoint.api.query;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 * ByTitle field equality query.
 */
public class ByTitle extends Query<String>
{
    public ByTitle(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByTitle query requires a non-empty value.");
        }

        setField(new DataObjectField("title"));
        setValue(value);
        setCollection(false);
    }

}

