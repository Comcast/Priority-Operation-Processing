package com.comcast.pop.cp.endpoint.progress.aws.persistence;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBAgendaProgressPersisterFactory extends DynamoDBConvertedPersisterFactory<AgendaProgress, PersistentAgendaProgress>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("linkid_index", "linkId");

    public DynamoDBAgendaProgressPersisterFactory()
    {
        super("id", AgendaProgress.class, new PersistentAgendaProgressConverter(), tableIndexes);
    }
}
