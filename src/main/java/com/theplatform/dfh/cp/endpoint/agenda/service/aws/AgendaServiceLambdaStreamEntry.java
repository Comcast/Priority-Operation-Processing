package com.theplatform.dfh.cp.endpoint.agenda.service.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.agenda.service.AgendaServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.aws.*;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.aws.sqs.AmazonSQSClientFactoryImpl;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaServiceLambdaStreamEntry extends AbstractLambdaStreamEntry<GetAgendaResponse, LambdaRequest<GetAgendaRequest>>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    private ItemQueueFactory<AgendaInfo> infoItemQueueFactory;
    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<GetAgendaRequest> lambdaRequest)
    {
        String insightTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT);
        //logger.info("TableName: {}", insightTableName);
        ObjectPersister<Insight> insightPersister = insightPersisterFactory.getObjectPersister(insightTableName);
        String agendaTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA);
        ObjectPersister<Agenda> agendaPersister = agendaPersisterFactory.getObjectPersister(agendaTableName);

        return new AgendaServiceRequestProcessor(infoItemQueueFactory, insightPersister, agendaPersister);
    }

    @Override
    public LambdaRequest<GetAgendaRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, GetAgendaRequest.class);
    }

    public AgendaServiceLambdaStreamEntry()
    {
        this.infoItemQueueFactory = new SQSItemQueueFactory<>(new AmazonSQSClientFactoryImpl().createClient(), ReadyAgenda.class);
        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
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