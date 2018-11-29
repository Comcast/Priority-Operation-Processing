package com.theplatform.dfh.endpoint.api.query;

import com.theplatform.dfh.persistence.api.DataTypeField;
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

        setField(new DataTypeField("title"));
        setValue(value);
        setCollection(false);
    }

}

