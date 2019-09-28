package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;

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
    public List<Insight> getInsights()
    {
        return super.getInsights();
    }
}