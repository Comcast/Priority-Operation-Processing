package com.theplatform.dfh.cp.endpoint.resourcepool.service.aws;

import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.EnvironmentLookupUtils;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.service.GetAgendaServiceRequestProcessor;
import com.comcast.pop.endpoint.aws.*;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.DataObjectFeedServiceResponse;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaRequest;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.aws.sqs.AmazonSQSClientFactoryImpl;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 * Main entry point class for the AWS getAgenda endpoint
 */
public class GetAgendaLambdaStreamEntry extends AbstractLambdaStreamEntry<DataObjectFeedServiceResponse<Agenda>, LambdaRequest<GetAgendaRequest>>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    private ItemQueueFactory<AgendaInfo> infoItemQueueFactory;
    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<GetAgendaRequest> lambdaRequest)
    {
        return new GetAgendaServiceRequestProcessor(infoItemQueueFactory,
            insightPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT)),
            agendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA)),
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)));
    }

    @Override
    public LambdaRequest<GetAgendaRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, GetAgendaRequest.class);
    }

    public GetAgendaLambdaStreamEntry()
    {
        this.infoItemQueueFactory = new SQSItemQueueFactory<>(new AmazonSQSClientFactoryImpl().createClient(), ReadyAgenda.class);
        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
        this.agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        this.operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
    }

    public ItemQueueFactory<AgendaInfo> getInfoItemQueueFactory()
    {
        return infoItemQueueFactory;
    }

    public void setInfoItemQueueFactory(ItemQueueFactory<AgendaInfo> infoItemQueueFactory)
    {
        this.infoItemQueueFactory = infoItemQueueFactory;
    }
}

