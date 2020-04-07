package com.theplatform.dfh.cp.endpoint.agendatemplate.aws.persistence;

import com.comcast.pop.api.AgendaTemplate;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

public class PersistentAgendaTemplateConverter extends DynamoDBPersistentObjectConverter<AgendaTemplate, PersistentAgendaTemplate>
{
    public PersistentAgendaTemplateConverter()
    {
        super(AgendaTemplate.class, PersistentAgendaTemplate.class);
    }
}