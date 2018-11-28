package com.theplatform.dfh.persistence.aws.dynamodb.api.progress;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.theplatform.dfh.cp.api.progress.OperationDiagnostics;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

import java.util.Date;

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

    @Override
    public String getAgendaProgressId()
    {
        return super.getAgendaProgressId();
    }

    @Override
    public String getProcessingStateMessage()
    {
        return super.getProcessingStateMessage();
    }

    @Override
    public String getOperation()
    {
        return super.getOperation();
    }

    @Override
    public int getAttemptCount()
    {
        return super.getAttemptCount();
    }

    @Override
    public Date getAttemptTime()
    {
        return super.getAttemptTime();
    }

    @Override
    public Date getStartedTime()
    {
        return super.getStartedTime();
    }

    @Override
    public Date getCompletedTime()
    {
        return super.getCompletedTime();
    }

    @Override
    public String getResultPayload()
    {
        return super.getResultPayload();
    }
}
