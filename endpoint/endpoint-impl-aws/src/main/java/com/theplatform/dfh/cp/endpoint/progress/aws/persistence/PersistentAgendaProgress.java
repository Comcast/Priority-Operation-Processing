package com.theplatform.dfh.cp.endpoint.progress.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.comcast.pop.api.AgendaInsight;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.PersistentAgendaInsightConverter;
import com.theplatform.dfh.cp.endpoint.persistence.DateConverter;
import com.theplatform.dfh.cp.endpoint.persistence.DiagnosticEventArrayConverter;
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
    @DynamoDBTypeConverted(converter = DiagnosticEventArrayConverter.class)
    @DynamoDBAttribute(attributeName = "diagnosticEvents")
    public DiagnosticEvent[] getDiagnosticEvents()
    {
        return super.getDiagnosticEvents();
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
    @DynamoDBTypeConverted(converter = PersistentAgendaInsightConverter.class)
    public AgendaInsight getAgendaInsight()
    {
        return super.getAgendaInsight();
    }

    @Override
    public Integer getAttemptsCompleted()
    {
        return super.getAttemptsCompleted();
    }

    @Override
    public Integer getMaximumAttempts()
    {
        return super.getMaximumAttempts();
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

