package com.theplatform.dfh.endpoint.api.query.scheduling;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 */
public class ByAgendaId extends Query<String>
{
    private static final String fieldName = "agendaId";
    public ByAgendaId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByAgendaId query requires a non-empty value.");
        }

        setField(new DataObjectField(fieldName));
        setValue(value);
        setCollection(false);
    }

    public static String fieldName()
    {
        return fieldName;
    }
}
