package com.comcast.pop.endpoint.agenda.aws.persistence;

import com.comcast.pop.endpoint.persistence.JsonDynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.AgendaInsight;

public class PersistentAgendaInsightConverter extends JsonDynamoDBTypeConverter<AgendaInsight>
{
    public PersistentAgendaInsightConverter()
    {
        super(new TypeReference<AgendaInsight>(){});
    }
}

