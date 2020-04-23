package com.comcast.pop.endpoint.api.data.query.progress;

import com.comcast.pop.persistence.api.field.DataObjectField;
import com.comcast.pop.persistence.api.query.Query;

/**
 */
public class ByAgendaProgressId extends Query<String>
{
    private static final DataObjectField field = new DataObjectField("agendaProgressId");
    public ByAgendaProgressId(String value)
    {
        if(value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("ByAgendaProgressId query requires a non-empty value.");
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
