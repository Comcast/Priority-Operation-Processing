package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.theplatform.dfh.cp.api.AgendaInsight;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

public class PersistentAgendaInsightConverter extends JsonDynamoDBTypeConverter<AgendaInsight>
{
    public PersistentAgendaInsightConverter()
    {
        super(new TypeReference<AgendaInsight>(){});
    }
}

