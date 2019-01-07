package com.theplatform.dfh.cp.endpoint.agenda.service.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.compression.zlib.ZlibUtil;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.agenda.service.AgendaProgressUpdater;
import com.theplatform.dfh.cp.endpoint.agenda.service.AgendaProgressUpdaterFactory;
import com.theplatform.dfh.cp.endpoint.agenda.service.AgendaServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.aws.*;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.aws.sqs.AmazonSQSClientFactoryImpl;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgendaServiceLambdaStreamEntry implements JsonRequestStreamHandler
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final JsonHelper jsonHelper = new JsonHelper();

    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private AgendaProgressUpdaterFactory agendaProgressUpdaterFactory = new AgendaProgressUpdaterFactory();

    private ItemQueueFactory<AgendaInfo> infoItemQueueFactory;
    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<Insight> insightPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;

    public AgendaServiceLambdaStreamEntry()
    {
        this.infoItemQueueFactory = new SQSItemQueueFactory<>(new AmazonSQSClientFactoryImpl().createClient(), ReadyAgenda.class);

        this.agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        this.insightPersisterFactory = new DynamoDBInsightPersisterFactory();
        this.agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        this.operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
    }

    protected AgendaServiceRequestProcessor getRequestProcessor(ObjectPersister<Insight> insightPersister, ObjectPersister<Agenda> agendaPersister)
    {
        return new AgendaServiceRequestProcessor(infoItemQueueFactory, insightPersister, agendaPersister);
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        // this is immediately made available for subclasses
        JsonNode rootRequestNode = jsonHelper.getObjectMapper().readTree(inputStream);
        handleRequest(rootRequestNode, outputStream, context);
    }

    @Override
    public void handleRequest(JsonNode inputStreamNode, OutputStream outputStream, Context context) throws IOException
    {
        logger.info("AgendaProvider endpoint request received.");
        JsonNode rootRequestNode = inputStreamNode;
        LambdaServiceRequest<GetAgendaRequest> lambdaRequest = new LambdaServiceRequest<>(rootRequestNode, GetAgendaRequest.class);

        String responseBody = null;
        int httpStatusCode = 200;

        String httpMethod = lambdaRequest.getHTTPMethod("UNKNOWN").toUpperCase();
        switch (httpMethod)
        {
            case "POST":
                responseBody = handlePost(lambdaRequest);
                break;
            default:
                // todo: some bad response code
                httpStatusCode = 405;
                logger.warn("Unsupported method type {}.", httpMethod);
        }
        logger.info("Response Body: {}", responseBody);
        String response = jsonHelper.getJSONString(new AWSLambdaStreamResponseObject(httpStatusCode,responseBody));
        logger.info("Response {}", response);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(response);
        writer.close();
    }

    public String handlePost(LambdaServiceRequest<GetAgendaRequest> lambdaRequest) throws IOException
    {
        // if no insights were provided, do the old mode (just send back any Agenda)
        // otherwise, do the new way with Insights
        String responseBody;
        GetAgendaRequest requestObject = lambdaRequest.getRequestObject();
        if (requestObject != null)
        {
            // create persisters for Insight and Agenda
            String insightTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.INSIGHT);
            logger.info("TableName: {}", insightTableName);
            ObjectPersister<Insight> insightPersister = insightPersisterFactory.getObjectPersister(insightTableName);
            String agendaTableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA);
            ObjectPersister<Agenda> agendaPersister = agendaPersisterFactory.getObjectPersister(agendaTableName);

            AgendaServiceRequestProcessor requestProcessor = getRequestProcessor(insightPersister, agendaPersister);
            GetAgendaResponse getAgendaResponse = requestProcessor.processRequest(requestObject);
            responseBody = jsonHelper.getJSONString(getAgendaResponse);
        }
        else
        {
            Agenda responseObject = handlePostNoInsight(
                agendaProgressUpdaterFactory.createAgendaProgressUpdater(
                    agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
                    operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS))
                ),
                lambdaRequest);
            responseBody = responseObject == null ? null : jsonHelper.getJSONString(responseObject);
        }
        return responseBody;
    }

    /**
     * Handles the POST of an object
     * @return Resulting id of the persisted object
     */
    public Agenda handlePostNoInsight(AgendaProgressUpdater agendaProgressUpdater, LambdaRequest lambdaRequest) throws IOException
    {
        String tableName = environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA);
        //logger.info("TableName: {}", tableName);
        ObjectPersister<Agenda> agendaPersister = new DynamoDBCompressedObjectPersister<>(tableName, "id", new AWSDynamoDBFactory(), Agenda.class);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_WEST_2).build();

        ScanRequest scanRequest = new ScanRequest()
            .withTableName(tableName)
            .withLimit(2);
        
        ScanResult result = client.scan(scanRequest);
        logger.info("Logging items...");
        List<Agenda> agendaList = new ArrayList<>();
        for (Map<String, AttributeValue> item : result.getItems()){
            logger.info("Item: {}", item);
            ByteBuffer blob = item.get("dataBlob").getB();
            String agendaString = new ZlibUtil().inflateMe(blob.array());
            logger.info("Agenda String: {}", agendaString);
            Agenda a = jsonHelper.getObjectFromString(agendaString, Agenda.class);
            agendaList.add(a);
        }

        if (agendaList.size() > 0)
        {
            logger.info("AgendaList contains {} items.", agendaList.size());
            Agenda response =  agendaList.get(0);

            // Update the progress
            agendaProgressUpdater.updateProgress(response);

            // todo this is temporary!  We should add a separate "claimAgenda" method (or something similiar) that GET's and DELETE's.
            logger.info("Deleting Agenda with id {}", response.getId());
            try
            {
                agendaPersister.delete(response.getId());
            }
            catch(PersistenceException e)
            {
                throw new BadRequestException(String.format("Unable to delete object by id {}", response.getId()), e);
            }

            return response;
        } else
        {
            logger.warn("Did not find any Agenda items in table {}.", tableName);
            return null;
        }
    }


    public void setAgendaProgressUpdaterFactory(AgendaProgressUpdaterFactory agendaProgressUpdaterFactory)
    {
        this.agendaProgressUpdaterFactory = agendaProgressUpdaterFactory;
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