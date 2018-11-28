package com.theplatform.dfh.persistence.aws.dynamodb.api.progress;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.theplatform.dfh.cp.api.progress.OperationDiagnostics;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

/**
 */
public class PersistentOperationProgress extends OperationProgress
{
    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }

    @Override
    @DynamoDBTypeConverted(converter = ProcessingStateConverter.class)
    public ProcessingState getProcessingState()
    {
        return super.getProcessingState();
    }

    @Override
    @DynamoDBIgnore
    public OperationDiagnostics[] getDiagnostics()
    {
        return super.getDiagnostics();
    }
}
