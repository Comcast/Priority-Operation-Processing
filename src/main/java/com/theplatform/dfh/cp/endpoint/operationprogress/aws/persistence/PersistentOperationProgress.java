package com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.persistence.DiagnosticEventArrayConverter;
import com.theplatform.dfh.cp.endpoint.persistence.ParamsMapConverter;

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
    public Date getUpdatedTime()
    {
        return super.getUpdatedTime();
    }

    @Override
    public Date getAddedTime()
    {
        return super.getAddedTime();
    }

    @Override
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    public ProcessingState getProcessingState()
    {
        return super.getProcessingState();
    }

    @Override
    @DynamoDBTypeConverted(converter = DiagnosticEventArrayConverter.class)
    @DynamoDBAttribute(attributeName = "diagnosticEvents")
    public DiagnosticEvent[] getDiagnosticEvents()
    {
        return super.getDiagnosticEvents();
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

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
    }

    @Override
    public void setCustomerId(String customerId)
    {
        super.setCustomerId(customerId);
    }

    @Override
    @DynamoDBTypeConverted(converter = ParamsMapConverter.class)
    @DynamoDBAttribute(attributeName = "params")
    public ParamsMap getParams()
    {
        return super.getParams();
    }

    @Override
    public void setParams(ParamsMap params)
    {
        super.setParams(params);
    }

    @Override
    public String getCid()
    {
        return super.getCid();
    }

}