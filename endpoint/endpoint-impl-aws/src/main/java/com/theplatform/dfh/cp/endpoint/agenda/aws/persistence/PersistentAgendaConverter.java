package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.PersistentAgendaProgress;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

public class PersistentAgendaConverter extends DynamoDBPersistentObjectConverter<Agenda, PersistentAgenda>
{
    public PersistentAgendaConverter()
    {
        super(Agenda.class, PersistentAgenda.class);
    }
}