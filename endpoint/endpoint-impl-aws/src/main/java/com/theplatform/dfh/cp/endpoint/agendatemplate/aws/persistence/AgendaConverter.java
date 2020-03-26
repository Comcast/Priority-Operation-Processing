package com.theplatform.dfh.cp.endpoint.agendatemplate.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

public class AgendaConverter extends JsonDynamoDBTypeConverter<Agenda>
{
    public AgendaConverter()
    {
        super(new TypeReference<Agenda>(){});
    }
}
