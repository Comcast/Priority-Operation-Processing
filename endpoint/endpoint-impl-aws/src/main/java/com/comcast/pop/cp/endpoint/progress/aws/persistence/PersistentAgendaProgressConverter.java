package com.comcast.pop.cp.endpoint.progress.aws.persistence;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentAgendaProgressConverter extends DynamoDBPersistentObjectConverter<AgendaProgress, PersistentAgendaProgress>
{
    public PersistentAgendaProgressConverter()
    {
        super(AgendaProgress.class, PersistentAgendaProgress.class);
    }
}