package com.theplatform.dfh.endpoint.api.query;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 * ById field equality query.
 */
public class ById extends Query<String>
{
    public ById(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ById query requires a non-empty value.");
        }

        setField(new DataObjectField("id"));
        setValue(value);
        setCollection(false);
    }

}

