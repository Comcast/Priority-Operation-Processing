package com.theplatform.dfh.cp.endpoint.operationprogress.aws;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.DataObjectLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.LambdaDataObjectRequest;
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