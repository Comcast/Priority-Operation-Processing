package com.comcast.pop.cp.endpoint.resourcepool.aws;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.ResourcePool;
import com.comcast.pop.cp.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.cp.endpoint.resourcepool.ResourcePoolRequestProcessor;
import com.comcast.pop.cp.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.comcast.pop.cp.endpoint.resourcepool.aws.persistence.PersistentResourcePoolConverter;
import com.comcast.pop.endpoint.aws.LambdaDataObjectRequest;
import com.comcast.pop.endpoint.aws.DataObjectLambdaStreamEntry;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.ObjectPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

/**
 * Main entry point class for the AWS Facility endpoint
 */
public class ResourcePoolLambdaStreamEntry extends DataObjectLambdaStreamEntry<ResourcePool>
{
    private static final TableIndexes tableIndexes = null;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;

    public ResourcePoolLambdaStreamEntry()
    {
        super(
            ResourcePool.class,
            new DynamoDBConvertedPersisterFactory<>("id", ResourcePool.class,
                new PersistentResourcePoolConverter(), tableIndexes));
        insightPersisterFactory = new DynamoDBInsightPersisterFactory();
    }

    @Override
    protected ResourcePoolRequestProcessor getRequestProcessor(LambdaDataObjectRequest<ResourcePool> lambdaRequest,
        ObjectPersister<ResourcePool> objectPersister)
    {
        return new ResourcePoolRequestProcessor(objectPersister,
            insightPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT)));
    }


    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.RESOURCE_POOL;
    }
}
