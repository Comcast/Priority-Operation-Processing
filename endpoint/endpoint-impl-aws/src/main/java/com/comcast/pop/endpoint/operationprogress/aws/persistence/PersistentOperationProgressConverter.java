package com.comcast.pop.endpoint.operationprogress.aws.persistence;

import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentOperationProgressConverter extends DynamoDBPersistentObjectConverter<OperationProgress, PersistentOperationProgress>
{
    public PersistentOperationProgressConverter()
    {
        super(OperationProgress.class, PersistentOperationProgress.class);
    }
}