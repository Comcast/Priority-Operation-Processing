package com.comcast.pop.cp.endpoint.agenda.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaInsight;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.cp.endpoint.persistence.DateConverter;
import com.comcast.pop.cp.endpoint.persistence.ParamsMapConverter;

import java.util.Date;
import java.util.List;

/**
 */
public class PersistentAgenda extends Agenda
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
    @DynamoDBTypeConverted(converter = ListOperationsConverter.class)
    @DynamoDBAttribute(attributeName = "operations")
    public List<Operation> getOperations()
    {
        return super.getOperations();
    }

    @Override
    @DynamoDBTypeConverted(converter = ParamsMapConverter.class)
    @DynamoDBAttribute(attributeName = "params")
    public ParamsMap getParams()
    {
        return super.getParams();
    }

    @Override
    public String getJobId()
    {
        return super.getJobId();
    }

    @Override
    public String getLinkId()
    {
        return super.getLinkId();
    }

    @Override
    public String getProgressId()
    {
        return super.getProgressId();
    }

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
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

    @Override
    @DynamoDBTypeConverted(converter = PersistentAgendaInsightConverter.class)
    @DynamoDBAttribute(attributeName = "agendaInsight")
    public AgendaInsight getAgendaInsight()
    {
        return super.getAgendaInsight();
    }
}
