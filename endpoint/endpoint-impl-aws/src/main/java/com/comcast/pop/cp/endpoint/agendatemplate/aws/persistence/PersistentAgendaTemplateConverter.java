package com.comcast.pop.cp.endpoint.agendatemplate.aws.persistence;

import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

public class PersistentAgendaTemplateConverter extends DynamoDBPersistentObjectConverter<AgendaTemplate, PersistentAgendaTemplate>
{
    public PersistentAgendaTemplateConverter()
    {
        super(AgendaTemplate.class, PersistentAgendaTemplate.class);
    }
}