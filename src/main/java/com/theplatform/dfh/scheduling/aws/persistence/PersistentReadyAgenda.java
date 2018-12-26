package com.theplatform.dfh.scheduling.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;

import java.util.Date;

public class PersistentReadyAgenda extends ReadyAgenda
{
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
    public Date getAdded()
    {
        return super.getAdded();
    }
}
