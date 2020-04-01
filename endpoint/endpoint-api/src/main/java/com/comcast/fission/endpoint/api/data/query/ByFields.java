package com.comcast.fission.endpoint.api.data.query;

import com.theplatform.dfh.persistence.api.field.FieldsField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 * ByFields query to specify the fields you want returned from the query.
 * Example : title,customerId
 */
public class ByFields extends Query<String>
{
    private static final FieldsField field = new FieldsField();
    public ByFields(String value)
    {
        setField(field);
        setValue(value);
        setCollection(false);
    }
    public static String fieldName()
    {
        return field.name();
    }

}

