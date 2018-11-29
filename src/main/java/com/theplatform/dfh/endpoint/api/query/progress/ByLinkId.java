package com.theplatform.dfh.endpoint.api.query.progress;

import com.theplatform.dfh.persistence.api.DataTypeField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 * ByLinkId field equality query.
 */
public class ByLinkId extends Query<String>
{
    public ByLinkId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByLinkId query requires a non-empty value.");
        }

        setField(new DataTypeField("linkId"));
        setValue(value);
        setCollection(false);
    }
}

