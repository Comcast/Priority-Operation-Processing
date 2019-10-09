package com.theplatform.dfh.cp.endpoint.resourcepool.service.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.aws.AbstractLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.endpoint.aws.LambdaRequest;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.DynamoDBCustomerPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.DynamoDBResourcePoolPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.service.CreateAgendaServiceRequestProcessor;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import com.theplatform.dfh.scheduling.aws.persistence.DynamoDbReadyAgendaPersisterFactory;

/**
 * Main entry point class for the AWS createAgenda endpoint
 */
public class CreateAgendaLambdaStreamEntry extends AbstractLambdaStreamEntry<CreateAgendaResponse, LambdaRequest<CreateAgendaRequest>>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;
    private ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;
    private ObjectPersisterFactory<Customer> customerPersisterFactory;
    private ObjectPersisterFactory<ResourcePool> resourcePoolPersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<CreateAgendaRequest> lambdaRequest)
    {
        String insightTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT);
        //logger.info("TableName: {}", insightTableName);
        ObjectPersister<Insight> insightPersister = insightPersisterFactory.getObjectPersister(insightTableName);

        String agendaTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA);
        ObjectPersister<Agenda> agendaPersister = agendaPersisterFactory.getObjectPersister(agendaTableName);

        String resourcePoolTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.RESOURCE_POOL);
        ObjectPersister<ResourcePool> resourcePoolPersister = resourcePoolPersisterFactory.getObjectPersister(resourcePoolTable);

        String customerTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.CUSTOMER);
        ObjectPersister<Customer> customerPersister = customerPersisterFactory.getObjectPersister(customerTable);

        String agendaProgressTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS);
        ObjectPersister<AgendaProgress> agendaProgressPersister = agendaProgressPersisterFactory.getObjectPersister(agendaProgressTable);

        String opProgressTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS);
        ObjectPersister<OperationProgress> opProgressPersister = operationProgressPersisterFactory.getObjectPersister(opProgressTable);

        String readyAgendaTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.READY_AGENDA);
        ObjectPersister<ReadyAgenda> readyAgendaPersister = readyAgendaPersisterFactory.getObjectPersister(readyAgendaTable);

        return new CreateAgendaServiceRequestProcessor(resourcePoolPersister, insightPersister, agendaPersister, customerPersister,
            agendaProgressPersister, opProgressPersister, readyAgendaPersister);
    }

    @Override
    public LambdaRequest<CreateAgendaRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, CreateAgendaRequest.class);
    }

    public CreateAgendaLambdaStreamEntry()
    {
        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
        this.agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        this.operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        this.readyAgendaPersisterFactory = new DynamoDbReadyAgendaPersisterFactory();
        this.customerPersisterFactory = new DynamoDBCustomerPersisterFactory();
        this.resourcePoolPersisterFactory = new DynamoDBResourcePoolPersisterFactory();
    }
}

