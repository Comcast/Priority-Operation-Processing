package com.theplatform.dfh.cp.endpoint.resourcepool.aws;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.LambdaDataObjectRequest;
import com.theplatform.dfh.cp.endpoint.aws.DataObjectLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.resourcepool.ResourcePoolRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.PersistentResourcePoolConverter;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

/**
 * Main entry point class for the AWS Facility endpoint
 */
public class ResourcePoolLambdaStreamEntry extends DataObjectLambdaStreamEntry<ResourcePool>
{
    private static final TableIndexes tableIndexes = null;
    public ResourcePoolLambdaStreamEntry()
    {
        super(
            ResourcePool.class,
            new DynamoDBConvertedPersisterFactory<>("id", ResourcePool.class,
                new PersistentResourcePoolConverter(), tableIndexes));
    }

    @Override
    protected ResourcePoolRequestProcessor getRequestProcessor(LambdaDataObjectRequest<ResourcePool> lambdaDataObjectRequest, ObjectPersister<ResourcePool> objectPersister)
    {
        return new ResourcePoolRequestProcessor(objectPersister);
    }


    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.RESOURCE_POOL;
    }
}
