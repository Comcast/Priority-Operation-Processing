package com.theplatform.dfh.scheduling.aws.persistence;

import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentReadyAgendaConverter extends DynamoDBPersistentObjectConverter<ReadyAgenda, PersistentReadyAgenda>
{
    public PersistentReadyAgendaConverter()
    {
        super(ReadyAgenda.class, PersistentReadyAgenda.class);
    }
}
