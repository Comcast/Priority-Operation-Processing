package com.theplatform.dfh.persistence.aws.dynamodb.api.progress;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

/**
 */
public class PersistentAgendaProgress extends AgendaProgress
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
    public OperationProgress[] getOperationProgress()
    {
        return super.getOperationProgress();
    }
}
