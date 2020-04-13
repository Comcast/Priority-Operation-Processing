package com.comcast.pop.endpoint.agenda.service.aws;

import com.comcast.pop.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.comcast.pop.endpoint.agenda.service.IgniteAgendaServiceRequestProcessor;
import com.comcast.pop.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.endpoint.agendatemplate.aws.persistence.DynamoDBAgendaTemplatePersisterFactory;
import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.EnvironmentLookupUtils;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.endpoint.resourcepool.aws.persistence.DynamoDBCustomerPersisterFactory;
import com.comcast.pop.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.agenda.IgniteAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.IgniteAgendaResponse;
import com.comcast.pop.persistence.api.ObjectPersisterFactory;
import com.comcast.pop.scheduling.aws.persistence.DynamoDbReadyAgendaPersisterFactory;

/**
 * Main entry point class for the AWS Agenda+payload submit endpoint
 */
public class IgniteAgendaLambdaStreamEntry extends AbstractLambdaStreamEntry<IgniteAgendaResponse, LambdaRequest<IgniteAgendaRequest>>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;
    private ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;
    private ObjectPersisterFactory<Customer> customerPersisterFactory;
    private ObjectPersisterFactory<AgendaTemplate> agendaTemplatePersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<IgniteAgendaRequest> lambdaRequest)
    {
        return new IgniteAgendaServiceRequestProcessor(
            insightPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT)),
            agendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA)),
            customerPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.CUSTOMER)),
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)),
            readyAgendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.READY_AGENDA)),
            agendaTemplatePersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_TEMPLATE)));
    }

    @Override
    public LambdaRequest<IgniteAgendaRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, IgniteAgendaRequest.class);
    }

    public IgniteAgendaLambdaStreamEntry()
    {
        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
        this.agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        this.operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        this.readyAgendaPersisterFactory = new DynamoDbReadyAgendaPersisterFactory();
        this.customerPersisterFactory = new DynamoDBCustomerPersisterFactory();
        this.agendaTemplatePersisterFactory = new DynamoDBAgendaTemplatePersisterFactory();
    }
}

