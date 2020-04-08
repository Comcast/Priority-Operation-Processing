package com.comcast.pop.cp.endpoint.agenda.aws.persistence;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

public class PersistentAgendaConverter extends DynamoDBPersistentObjectConverter<Agenda, PersistentAgenda>
{
    public PersistentAgendaConverter()
    {
        super(Agenda.class, PersistentAgenda.class);
    }
}