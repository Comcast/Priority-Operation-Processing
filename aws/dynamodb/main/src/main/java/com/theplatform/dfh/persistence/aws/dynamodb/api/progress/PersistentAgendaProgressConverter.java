package com.theplatform.dfh.persistence.aws.dynamodb.api.progress;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
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
