package com.comcast.pop.cp.endpoint.agenda.service.aws;

import com.comcast.pop.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.comcast.pop.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.cp.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.cp.endpoint.agenda.service.ReigniteAgendaServiceRequestProcessor;
import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.EnvironmentLookupUtils;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.cp.endpoint.resourcepool.aws.persistence.DynamoDBCustomerPersisterFactory;
import com.comcast.pop.cp.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaResponse;
import com.comcast.pop.persistence.api.ObjectPersisterFactory;
import com.comcast.pop.scheduling.aws.persistence.DynamoDbReadyAgendaPersisterFactory;

/**
 * Main entry point class for the AWS Agenda retry endpoint
 */
public class ReigniteAgendaLambdaStreamEntry extends AbstractLambdaStreamEntry<ReigniteAgendaResponse, LambdaRequest<ReigniteAgendaRequest>>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;
    private ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;
    private ObjectPersisterFactory<Customer> customerPersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<ReigniteAgendaRequest> lambdaRequest)
    {
        return new ReigniteAgendaServiceRequestProcessor(
            agendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA)),
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)),
            readyAgendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.READY_AGENDA)),
            insightPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT)),
            customerPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.CUSTOMER))
        );
    }

    @Override
    public LambdaRequest<ReigniteAgendaRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, ReigniteAgendaRequest.class);
    }

    public ReigniteAgendaLambdaStreamEntry()
    {
        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
        this.agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        this.operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        this.readyAgendaPersisterFactory = new DynamoDbReadyAgendaPersisterFactory();
        this.customerPersisterFactory = new DynamoDBCustomerPersisterFactory();
    }
}

