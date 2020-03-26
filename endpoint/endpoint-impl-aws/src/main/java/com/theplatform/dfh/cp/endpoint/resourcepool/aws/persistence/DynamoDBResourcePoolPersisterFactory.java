package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

/**
 */
public class DynamoDBResourcePoolPersisterFactory extends DynamoDBConvertedPersisterFactory<ResourcePool, PersistentResourcePool>
{
    protected static final TableIndexes tableIndexes = new TableIndexes();

    public DynamoDBResourcePoolPersisterFactory()
    {
        super("id", ResourcePool.class, new PersistentResourcePoolConverter(), tableIndexes);
    }
}
