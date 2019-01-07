package com.theplatform.dfh.cp.endpoint.progress.aws;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * Main entry point class for the AWS Agenda endpoint
 */
public class ProgressLambdaStreamEntry extends BaseAWSLambdaStreamEntry<AgendaProgress>
{
    private DynamoDBOperationProgressPersisterFactory operationProgressPersisterFactory;

    public ProgressLambdaStreamEntry()
    {
        super(
            AgendaProgress.class,
            new DynamoDBAgendaProgressPersisterFactory()
        );
        operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
    }

    @Override
    protected AgendaProgressRequestProcessor getRequestProcessor(LambdaObjectRequest<AgendaProgress> lambdaObjectRequest, ObjectPersister<AgendaProgress> objectPersister)
    {
        String authHeader = lambdaObjectRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        return new AgendaProgressRequestProcessor(
            objectPersister,
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaObjectRequest, TableEnvironmentVariableName.OPERATION_PROGRESS))
        );
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.AGENDA_PROGRESS;
    }
}
