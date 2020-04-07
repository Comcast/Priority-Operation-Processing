package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.ResourcePool;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentResourcePoolConverter extends DynamoDBPersistentObjectConverter<ResourcePool, PersistentResourcePool>
{
    public PersistentResourcePoolConverter()
    {
        super(ResourcePool.class, PersistentResourcePool.class);
    }
}
