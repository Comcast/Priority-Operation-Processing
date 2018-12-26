package com.theplatform.dfh.cp.endpoint.operationprogress.aws;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 */
public class OperationProgressLambdaStreamEntry extends BaseAWSLambdaStreamEntry<OperationProgress>
{
    public OperationProgressLambdaStreamEntry()
    {
        super(
            OperationProgress.class,
            new DynamoDBOperationProgressPersisterFactory()
        );
    }

    @Override
    protected OperationProgressRequestProcessor getRequestProcessor(LambdaObjectRequest<OperationProgress> lambdaRequest, ObjectPersister<OperationProgress> objectPersister)
    {
        return new OperationProgressRequestProcessor(objectPersister);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.OPERATION_PROGRESS;
    }
}