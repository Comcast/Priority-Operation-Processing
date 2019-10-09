package com.theplatform.dfh.cp.endpoint.resourcepool.service.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.service.GetAgendaServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.aws.*;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence.DynamoDBResourcePoolPersisterFactory;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.DataObjectFeedServiceResponse;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.aws.sqs.AmazonSQSClientFactoryImpl;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
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
    private ObjectPersisterFactory<ResourcePool> resourcePoolPersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<GetAgendaRequest> lambdaRequest)
    {
        String insightTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT);
        //logger.info("TableName: {}", insightTableName);
        ObjectPersister<Insight> insightPersister = insightPersisterFactory.getObjectPersister(insightTableName);
        String agendaTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA);
        ObjectPersister<Agenda> agendaPersister = agendaPersisterFactory.getObjectPersister(agendaTableName);

        return new GetAgendaServiceRequestProcessor(infoItemQueueFactory, insightPersister, agendaPersister);
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
        this.resourcePoolPersisterFactory = new DynamoDBResourcePoolPersisterFactory();
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

