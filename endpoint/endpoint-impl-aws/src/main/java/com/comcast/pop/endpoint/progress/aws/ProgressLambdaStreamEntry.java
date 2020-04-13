package com.comcast.pop.endpoint.progress.aws;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.comcast.pop.endpoint.aws.LambdaDataObjectRequest;
import com.comcast.pop.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.endpoint.aws.DataObjectLambdaStreamEntry;
import com.comcast.pop.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.comcast.pop.persistence.api.ObjectPersister;

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
