package com.theplatform.dfh.cp.endpoint.operationprogress.aws;

import com.comcast.pop.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.endpoint.aws.DataObjectLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.LambdaDataObjectRequest;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;

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