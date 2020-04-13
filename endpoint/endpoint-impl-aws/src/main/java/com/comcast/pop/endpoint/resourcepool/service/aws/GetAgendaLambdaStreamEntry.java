package com.comcast.pop.endpoint.resourcepool.service.aws;

import com.comcast.pop.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.comcast.pop.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.comcast.pop.endpoint.resourcepool.service.GetAgendaServiceRequestProcessor;
import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.EnvironmentLookupUtils;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.comcast.pop.scheduling.api.AgendaInfo;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.DataObjectFeedServiceResponse;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaRequest;
import com.comcast.pop.modules.queue.api.ItemQueueFactory;
import com.comcast.pop.modules.queue.aws.sqs.AmazonSQSClientFactoryImpl;
import com.comcast.pop.modules.queue.aws.sqs.SQSItemQueueFactory;
import com.comcast.pop.persistence.api.ObjectPersisterFactory;

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

