package com.theplatform.dfh.persistence.api.query;

import com.theplatform.dfh.persistence.api.DataTypeField;

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

        setField(new DataTypeField("id"));
        setValue(value);
        setCollection(false);
    }

}

