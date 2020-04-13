package com.comcast.pop.endpoint.agendatemplate.aws.persistence;

import com.comcast.pop.endpoint.persistence.JsonDynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.Agenda;

public class AgendaConverter extends JsonDynamoDBTypeConverter<Agenda>
{
    public AgendaConverter()
    {
        super(new TypeReference<Agenda>(){});
    }
}
