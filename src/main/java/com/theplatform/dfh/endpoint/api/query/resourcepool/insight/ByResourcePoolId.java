package com.theplatform.dfh.endpoint.api.query.resourcepool.insight;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

public class ByResourcePoolId extends Query<String>
{
    public ByResourcePoolId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ById query requires a non-empty value.");
        }

        setField(new DataObjectField("resourcePoolId"));
        setValue(value);
        setCollection(false);
    }
}
