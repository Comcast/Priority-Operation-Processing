package com.comcast.pop.endpoint.operationprogress.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.persistence.DiagnosticEventArrayConverter;
import com.comcast.pop.endpoint.persistence.DateConverter;
import com.comcast.pop.endpoint.persistence.ParamsMapConverter;

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
    @DynamoDBTypeConverted(converter = DateConverter.class)
    public Date getUpdatedTime()
    {
        return super.getUpdatedTime();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
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
    public Integer getAttemptCount()
    {
        return super.getAttemptCount();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
    public Date getAttemptTime()
    {
        return super.getAttemptTime();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
    public Date getStartedTime()
    {
        return super.getStartedTime();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
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
    public Double getPercentComplete()
    {
        return super.getPercentComplete();
    }

    @Override
    public String getTitle()
    {
        return super.getTitle();
    }

    @Override
    public String getCid()
    {
        return super.getCid();
    }

}