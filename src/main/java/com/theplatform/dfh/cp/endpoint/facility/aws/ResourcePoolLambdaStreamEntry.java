package com.theplatform.dfh.cp.endpoint.facility.aws;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.facility.ResourcePoolRequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.PersistentResourcePoolConverter;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

/**
 * Main entry point class for the AWS Facility endpoint
 */
public class ResourcePoolLambdaStreamEntry extends BaseAWSLambdaStreamEntry<ResourcePool>
{
    private static final TableIndexes tableIndexes = null;
    public ResourcePoolLambdaStreamEntry()
    {
        super(
            ResourcePool.class,
            new DynamoDBConvertedPersisterFactory("id", ResourcePool.class,
                new PersistentResourcePoolConverter(), tableIndexes));
    }

    @Override
    protected ResourcePoolRequestProcessor getRequestProcessor(LambdaObjectRequest<ResourcePool> lambdaObjectRequest, ObjectPersister<ResourcePool> objectPersister)
    {
        return new ResourcePoolRequestProcessor(objectPersister);
    }


    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.RESOURCE_POOL;
    }
}
