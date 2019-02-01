package com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.persistence.aws.dynamodb.*;

/**
 */
public class DynamoDBOperationProgressPersisterFactory extends DynamoDBConvertedPersisterFactory<OperationProgress>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("agendaprogressid_index", "agendaProgressId");

    public DynamoDBOperationProgressPersisterFactory()
    {

        super("id", OperationProgress.class, new PersistentOperationProgressConverter(), tableIndexes);
    }
}