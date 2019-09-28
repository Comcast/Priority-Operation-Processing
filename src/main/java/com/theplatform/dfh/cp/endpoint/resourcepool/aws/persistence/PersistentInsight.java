package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.SchedulingAlgorithm;

import java.util.Map;
import java.util.Set;

/**
 */
public class PersistentInsight extends Insight
{

    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }

    @Override
    public String getResourcePoolId()
    {
        return super.getResourcePoolId();
    }

    @Override
    public String getQueueName()
    {
        return super.getQueueName();
    }

    @Override
    public int getQueueSize()
    {
        return super.getQueueSize();
    }

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
    }

    @Override
    public Map<String, Set<String>> getMappers()
    {
        return super.getMappers();
    }

    @Override
    public Set<String> getAllowedCustomerIds()
    {
        return super.getAllowedCustomerIds();
    }

    @Override
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    public SchedulingAlgorithm getSchedulingAlgorithm()
    {
        return super.getSchedulingAlgorithm();
    }

    @Override
    public String getTitle()
    {
        return super.getTitle();
    }
}
