package com.comcast.pop.cp.endpoint.agenda.aws;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.comcast.pop.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.comcast.pop.cp.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.cp.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.endpoint.aws.DataObjectLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.LambdaDataObjectRequest;
import com.comcast.pop.cp.endpoint.resourcepool.aws.persistence.DynamoDBCustomerPersisterFactory;
import com.comcast.pop.cp.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.comcast.pop.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.ObjectPersisterFactory;
import com.comcast.pop.scheduling.aws.persistence.DynamoDbReadyAgendaPersisterFactory;

/**
 * Main entry point class for the AWS Agenda endpoint
 */
public class AgendaLambdaStreamEntry extends DataObjectLambdaStreamEntry<Agenda>
{
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;
    private ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;
    private ObjectPersisterFactory<Customer> customerPersisterFactory;

    public AgendaLambdaStreamEntry()
    {
        super(
            Agenda.class,
            new DynamoDBAgendaPersisterFactory()
        );
        agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        readyAgendaPersisterFactory = new DynamoDbReadyAgendaPersisterFactory();
        insightPersisterFactory = new DynamoDBInsightPersisterFactory();
        customerPersisterFactory = new DynamoDBCustomerPersisterFactory();
    }

    @Override
    protected AgendaRequestProcessor getRequestProcessor(LambdaDataObjectRequest<Agenda> lambdaRequest, ObjectPersister<Agenda> objectPersister)
    {
        String authHeader = lambdaRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        return new AgendaRequestProcessor(objectPersister,
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            readyAgendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.READY_AGENDA)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)),
            insightPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT)),
            customerPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.CUSTOMER))
        );
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.AGENDA;
    }
}
