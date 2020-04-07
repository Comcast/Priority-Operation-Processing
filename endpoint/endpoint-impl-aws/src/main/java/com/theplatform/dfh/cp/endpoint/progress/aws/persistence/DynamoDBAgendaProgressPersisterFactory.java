package com.theplatform.dfh.cp.endpoint.progress.aws.persistence;

import com.comcast.pop.api.progress.AgendaProgress;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBAgendaProgressPersisterFactory extends DynamoDBConvertedPersisterFactory<AgendaProgress, PersistentAgendaProgress>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("linkid_index", "linkId");

    public DynamoDBAgendaProgressPersisterFactory()
    {
        super("id", AgendaProgress.class, new PersistentAgendaProgressConverter(), tableIndexes);
    }
}
