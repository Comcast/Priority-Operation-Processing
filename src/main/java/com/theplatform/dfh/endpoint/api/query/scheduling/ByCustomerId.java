package com.theplatform.dfh.endpoint.api.query.scheduling;

import com.theplatform.dfh.persistence.api.DataTypeField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 * ByCustomerId field equality query.
 */
public class ByCustomerId extends Query<String>
{
    public ByCustomerId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByCustomerId query requires a non-empty value.");
        }

        setField(new DataTypeField("customerId"));
        setValue(value);
        setCollection(false);
    }
}
