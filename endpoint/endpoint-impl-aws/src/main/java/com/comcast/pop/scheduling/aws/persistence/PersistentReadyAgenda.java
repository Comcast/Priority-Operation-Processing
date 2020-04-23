package com.comcast.pop.scheduling.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.comcast.pop.endpoint.persistence.DateConverter;
import com.comcast.pop.scheduling.api.ReadyAgenda;

import java.util.Date;

public class PersistentReadyAgenda extends ReadyAgenda
{
    // NOTE: This composite field is a storage only field for the sake of indexing
    private String insightIdCustomerIdComposite;

    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }

    @Override
    public String getInsightId()
    {
        return super.getInsightId();
    }

    @Override
    public String getAgendaId()
    {
        return super.getAgendaId();
    }

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
    public Date getAdded()
    {
        return super.getAdded();
    }

    public String getInsightIdCustomerIdComposite()
    {
        return insightIdCustomerIdComposite;
    }

    public void setInsightIdCustomerIdComposite(String insightIdCustomerIdComposite)
    {
        this.insightIdCustomerIdComposite = insightIdCustomerIdComposite;
    }
}
