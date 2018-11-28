package com.theplatform.dfh.persistence.aws.dynamodb.api.progress;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentOperationProgressConverter extends DynamoDBPersistentObjectConverter<OperationProgress, PersistentOperationProgress>
{
    public PersistentOperationProgressConverter()
    {
        super(OperationProgress.class, PersistentOperationProgress.class);
    }
}