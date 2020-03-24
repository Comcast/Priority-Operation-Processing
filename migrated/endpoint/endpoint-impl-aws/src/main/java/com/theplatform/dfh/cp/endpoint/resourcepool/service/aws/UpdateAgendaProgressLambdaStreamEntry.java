package com.theplatform.dfh.cp.endpoint.resourcepool.service.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
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
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.service.UpdateAgendaProgressServiceRequestProcessor;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 * Main entry point class for the AWS updateAgendaProgress endpoint
 */
public class UpdateAgendaProgressLambdaStreamEntry extends AbstractLambdaStreamEntry<UpdateAgendaProgressResponse, LambdaRequest<UpdateAgendaProgressRequest>>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<UpdateAgendaProgressRequest> lambdaRequest)
    {
        String agendaTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA);
        ObjectPersister<Agenda> agendaPersister = agendaPersisterFactory.getObjectPersister(agendaTableName);

        String agendaProgressTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS);
        ObjectPersister<AgendaProgress> agendaProgressPersister = agendaProgressPersisterFactory.getObjectPersister(agendaProgressTable);

        String opProgressTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS);
        ObjectPersister<OperationProgress> opProgressPersister = operationProgressPersisterFactory.getObjectPersister(opProgressTable);

        String insightTable = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT);
        ObjectPersister<Insight> insightPersister = insightPersisterFactory.getObjectPersister(insightTable);

        return new UpdateAgendaProgressServiceRequestProcessor(agendaProgressPersister, agendaPersister, opProgressPersister, insightPersister);
    }

    @Override
    public LambdaRequest<UpdateAgendaProgressRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, UpdateAgendaProgressRequest.class);
    }

    public UpdateAgendaProgressLambdaStreamEntry()
    {
        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        this.operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
    }
}

