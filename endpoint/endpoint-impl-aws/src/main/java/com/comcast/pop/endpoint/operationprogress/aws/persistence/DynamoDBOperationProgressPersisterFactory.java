package com.comcast.pop.endpoint.operationprogress.aws.persistence;

import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

/**
 */
public class DynamoDBOperationProgressPersisterFactory extends DynamoDBConvertedPersisterFactory<OperationProgress, PersistentOperationProgress>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("agendaprogressid_index", "agendaProgressId");

    public DynamoDBOperationProgressPersisterFactory()
    {

        super("id", OperationProgress.class, new PersistentOperationProgressConverter(), tableIndexes);
    }
}