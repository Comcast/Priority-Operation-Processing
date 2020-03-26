package com.theplatform.dfh.cp.endpoint.progress.aws;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.aws.LambdaDataObjectRequest;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.aws.DataObjectLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * Main entry point class for the AWS Agenda endpoint
 */
public class ProgressLambdaStreamEntry extends DataObjectLambdaStreamEntry<AgendaProgress>
{
    private DynamoDBOperationProgressPersisterFactory operationProgressPersisterFactory;
    private DynamoDBAgendaPersisterFactory agendaPersisterFactory;

    public ProgressLambdaStreamEntry()
    {
        super(
            AgendaProgress.class,
            new DynamoDBAgendaProgressPersisterFactory()
        );
        operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
    }

    @Override
    protected AgendaProgressRequestProcessor getRequestProcessor(LambdaDataObjectRequest<AgendaProgress> lambdaDataObjectRequest, ObjectPersister<AgendaProgress> agendaProgressPersister)
    {
        String authHeader = lambdaDataObjectRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        return new AgendaProgressRequestProcessor(
            agendaProgressPersister,
            agendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaDataObjectRequest, TableEnvironmentVariableName.AGENDA)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaDataObjectRequest, TableEnvironmentVariableName.OPERATION_PROGRESS))
        );
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.AGENDA_PROGRESS;
    }
}
