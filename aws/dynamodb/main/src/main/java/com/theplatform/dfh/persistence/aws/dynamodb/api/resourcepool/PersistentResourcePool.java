package com.theplatform.dfh.persistence.aws.dynamodb.api.resourcepool;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.theplatform.dfh.cp.api.facility.ResourcePool;

public class PersistentResourcePool extends ResourcePool
{
    
    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }
}
