package com.theplatform.dfh.cp.endpoint.progress.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.persistence.ParamsMapConverter;

import java.util.Date;

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
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
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

    @Override
    public String getAgendaId()
    {
        return super.getAgendaId();
    }

    @Override
    public String getLinkId()
    {
        return super.getLinkId();
    }

    @Override
    public String getExternalId()
    {
        return super.getExternalId();
    }

    @Override
    public String getTitle()
    {
        return super.getTitle();
    }

    @Override
    public String getProcessingStateMessage()
    {
        return super.getProcessingStateMessage();
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
    public Double getPercentComplete()
    {
        return super.getPercentComplete();
    }

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
    }

    @Override
    public String getCid()
    {
        return super.getCid();
    }

    @Override
    @DynamoDBTypeConverted(converter = ParamsMapConverter.class)
    @DynamoDBAttribute(attributeName = "params")
    public ParamsMap getParams()
    {
        return super.getParams();
    }
}

