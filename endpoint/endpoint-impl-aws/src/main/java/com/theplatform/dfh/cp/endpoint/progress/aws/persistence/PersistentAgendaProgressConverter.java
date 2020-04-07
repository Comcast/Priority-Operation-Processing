package com.theplatform.dfh.cp.endpoint.progress.aws.persistence;

import com.comcast.pop.api.progress.AgendaProgress;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentAgendaProgressConverter extends DynamoDBPersistentObjectConverter<AgendaProgress, PersistentAgendaProgress>
{
    public PersistentAgendaProgressConverter()
    {
        super(AgendaProgress.class, PersistentAgendaProgress.class);
    }
}