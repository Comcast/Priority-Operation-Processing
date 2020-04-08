package com.comcast.pop.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.ResourcePool;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

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
