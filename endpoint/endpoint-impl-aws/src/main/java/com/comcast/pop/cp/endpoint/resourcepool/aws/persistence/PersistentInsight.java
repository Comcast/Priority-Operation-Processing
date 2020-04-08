package com.comcast.pop.cp.endpoint.resourcepool.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.SchedulingAlgorithm;
import com.comcast.pop.cp.endpoint.persistence.DateConverter;

import java.util.Date;
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
    public Integer getQueueSize()
    {
        return super.getQueueSize();
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
    public Boolean getIsGlobal()
    {
        return super.getIsGlobal();
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
