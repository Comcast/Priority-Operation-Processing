package com.comcast.pop.cp.endpoint.operationprogress.aws;

import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.cp.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.comcast.pop.endpoint.aws.DataObjectLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.LambdaDataObjectRequest;
import com.comcast.pop.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.persistence.api.ObjectPersister;

/**
 */
public class OperationProgressLambdaStreamEntry extends DataObjectLambdaStreamEntry<OperationProgress>
{
    public OperationProgressLambdaStreamEntry()
    {
        super(
            OperationProgress.class,
            new DynamoDBOperationProgressPersisterFactory()
        );
    }

    @Override
    protected OperationProgressRequestProcessor getRequestProcessor(LambdaDataObjectRequest<OperationProgress> lambdaRequest, ObjectPersister<OperationProgress> objectPersister)
    {
        return new OperationProgressRequestProcessor(objectPersister);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.OPERATION_PROGRESS;
    }
}