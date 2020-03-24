package com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

public class ByResourcePoolId extends Query<String>
{
    private static final DataObjectField field = new DataObjectField("resourcePoolId");
    public ByResourcePoolId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("By Query requires a non-empty value.");
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
