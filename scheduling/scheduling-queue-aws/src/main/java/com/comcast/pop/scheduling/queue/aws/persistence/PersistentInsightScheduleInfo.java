package com.comcast.pop.scheduling.queue.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.comcast.pop.scheduling.queue.InsightScheduleInfo;

import java.util.Date;
import java.util.List;

/**
 */
public class PersistentInsightScheduleInfo extends InsightScheduleInfo
{
    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }

    @Override
    public Date getLastExecuted()
    {
        return super.getLastExecuted();
    }

    @Override
    public List<String> getPendingCustomerIds()
    {
        return super.getPendingCustomerIds();
    }

    @Override
    public String getLastMessage()
    {
        return super.getLastMessage();
    }
}
