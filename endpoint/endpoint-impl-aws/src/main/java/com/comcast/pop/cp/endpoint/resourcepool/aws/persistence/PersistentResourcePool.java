package com.comcast.pop.cp.endpoint.resourcepool.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.comcast.pop.api.facility.ResourcePool;
import com.comcast.pop.cp.endpoint.persistence.DateConverter;

import java.util.Date;
import java.util.List;

/**
 */
public class PersistentResourcePool extends ResourcePool
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
    public String getCid()
    {
        return super.getCid();
    }

    @Override
    public String getTitle()
    {
        return super.getTitle();
    }

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
    }

    @Override
    @DynamoDBIgnore
    public List<String> getInsightIds()
    {
        return super.getInsightIds();
    }
}