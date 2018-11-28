package com.theplatform.dfh.persistence.aws.dynamodb.api.resourcepool;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

public class PersistentResourcePoolConverter extends DynamoDBPersistentObjectConverter<ResourcePool, PersistentResourcePool>
{
    public PersistentResourcePoolConverter()
    {
        super(ResourcePool.class, PersistentResourcePool.class);
    }
}
