package com.comcast.pop.endpoint.agenda.service.aws;

import com.comcast.pop.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.comcast.pop.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.endpoint.agenda.service.RerunAgendaServiceRequestProcessor;
import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.EnvironmentLookupUtils;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.endpoint.resourcepool.aws.persistence.DynamoDBCustomerPersisterFactory;
import com.comcast.pop.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaResponse;
import com.comcast.pop.persistence.api.ObjectPersisterFactory;
import com.comcast.pop.scheduling.aws.persistence.DynamoDbReadyAgendaPersisterFactory;

/**
 * Main entry point class for the AWS Agenda retry endpoint
 */
public class RerunAgendaLambdaStreamEntry extends AbstractLambdaStreamEntry<RerunAgendaResponse, LambdaRequest<RerunAgendaRequest>>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;
    private ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;
    private ObjectPersisterFactory<Customer> customerPersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<RerunAgendaRequest> lambdaRequest)
    {
        return new RerunAgendaServiceRequestProcessor(
            agendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA)),
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)),
            readyAgendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.READY_AGENDA)),
            insightPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT)),
            customerPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.CUSTOMER))
        );
    }

    @Override
    public LambdaRequest<RerunAgendaRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, RerunAgendaRequest.class);
    }

    public RerunAgendaLambdaStreamEntry()
    {
        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
        this.agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        this.operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        this.readyAgendaPersisterFactory = new DynamoDbReadyAgendaPersisterFactory();
        this.customerPersisterFactory = new DynamoDBCustomerPersisterFactory();
    }
}

