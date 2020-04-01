package com.comcast.fission.endpoint.api.data.query.progress;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

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
