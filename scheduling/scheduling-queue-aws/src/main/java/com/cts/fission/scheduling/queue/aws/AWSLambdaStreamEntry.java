package com.cts.fission.scheduling.queue.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.comcast.fission.endpoint.api.BadRequestException;
import com.comcast.fission.endpoint.api.data.query.resourcepool.insight.ByInsightId;
import com.comcast.fission.endpoint.api.data.query.scheduling.ByInsightIdCustomerId;
import com.cts.fission.scheduling.queue.aws.persistence.PersistentInsightScheduleInfoConverter;
import com.cts.fission.scheduling.queue.monitor.QueueMonitor;
import com.cts.fission.scheduling.queue.monitor.QueueMonitorFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.cts.fission.scheduling.queue.InsightScheduleInfo;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.http.api.AuthHttpURLConnectionFactory;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.aws.sqs.AmazonSQSClientFactoryImpl;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;
import com.theplatform.dfh.scheduling.aws.persistence.PersistentReadyAgendaConverter;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Main entry point class for a CloudWatch Event trigger
 *
 * The incoming request from an event is whatever is specified in the event (assuming constant JSON text)
 */
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String ENV_ENDPOINT_URL = "ENDPOINT_URL";
    private final String ENV_INSIGHT_ENDPOINT_PATH = "INSIGHT_ENDPOINT_PATH";
    private final String ENV_CUSTOMER_ENDPOINT_PATH = "CUSTOMER_ENDPOINT_PATH";
    private final String ENV_INSIGHT_SCHEDULING_INFO_TABLE = "INSIGHT_SCHEDULING_INFO_TABLE";
    private final String ENV_READY_AGENDA_TABLE = "READY_AGENDA_TABLE";

    private final ObjectPersisterFactory<InsightScheduleInfo> insightScheduleInfoPersisterFactory;
    private final ItemQueueFactory<ReadyAgenda> readyAgendaQueueFactory;
    private final ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;

    private QueueMonitorFactory queueMonitorFactory = new QueueMonitorFactory();
    private EnvironmentFacade environmentFacade = new EnvironmentFacade();
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    public AWSLambdaStreamEntry()
    {
        this(
            new DynamoDBConvertedPersisterFactory<>("id", InsightScheduleInfo.class, new PersistentInsightScheduleInfoConverter(), null),
            new SQSItemQueueFactory<>(new AmazonSQSClientFactoryImpl().createClient(), ReadyAgenda.class),
            new DynamoDBConvertedPersisterFactory<>("id", ReadyAgenda.class,
                new PersistentReadyAgendaConverter(),
                new TableIndexes()
                    .withIndex("insightIdCustomerIdComposite_added_index", ByInsightIdCustomerId.fieldName())
                    .withIndex("insightid_added_index", ByInsightId.fieldName()))
        );
    }

    public AWSLambdaStreamEntry(
        ObjectPersisterFactory<InsightScheduleInfo> insightScheduleInfoPersisterFactory,
        ItemQueueFactory<ReadyAgenda> readyAgendaQueueFactory,
        ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory
    )
    {
        this.insightScheduleInfoPersisterFactory = insightScheduleInfoPersisterFactory;
        this.readyAgendaQueueFactory = readyAgendaQueueFactory;
        this.readyAgendaPersisterFactory = readyAgendaPersisterFactory;
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger, false);

        ResourcePoolSchedulerRequest request;
        try
        {
            request = objectMapper.readValue(inputStream, ResourcePoolSchedulerRequest.class);
        }
        catch(IOException e)
        {
            throw new BadRequestException("Failed to read input as ResourcePoolSchedulerRequest.", e);
        }
        if(request == null || request.getResourcePoolId() == null || request.getStageId() == null)
        {
            throw new BadRequestException("Request must have a facilityId and stageId.");
        }

        String endpointURL = getEnvironmentVar(ENV_ENDPOINT_URL);
        processResourcePool(request, endpointURL);
    }

    private void processResourcePool(ResourcePoolSchedulerRequest request, String endpointURL)
    {
        String insightEndpointPath = getEnvironmentVar(ENV_INSIGHT_ENDPOINT_PATH);
        String customerEndpointPath = getEnvironmentVar(ENV_CUSTOMER_ENDPOINT_PATH);
        String insightSchedulingInfoTablePrefix = getEnvironmentVar(ENV_INSIGHT_SCHEDULING_INFO_TABLE);
        String readyAgendaTablePrefix = getEnvironmentVar(ENV_READY_AGENDA_TABLE);

        HttpObjectClient<Insight> insightClient = new HttpObjectClient<>(
            environmentLookupUtils.getAPIEndpointURL(endpointURL, request.getStageId(), insightEndpointPath),
            getHttpURLConnectionFactory(),
            Insight.class);

        HttpObjectClient<Customer> customerClient = new HttpObjectClient<>(
            environmentLookupUtils.getAPIEndpointURL(endpointURL, request.getStageId(), customerEndpointPath),
            getHttpURLConnectionFactory(),
            Customer.class);

        final String readyAgendaTableName = environmentLookupUtils.getTableName(readyAgendaTablePrefix, request.getStageId());
        final String scheduleInfoTableName = environmentLookupUtils.getTableName(insightSchedulingInfoTablePrefix, request.getStageId());

        logger.info("ReadyAgenda Table: {} ScheduleInfo Table: {}", readyAgendaTableName, scheduleInfoTableName);

        QueueMonitor queueMonitor = queueMonitorFactory.createQueueMonitor(
            readyAgendaQueueFactory,
            readyAgendaPersisterFactory.getObjectPersister(readyAgendaTableName),
            insightClient,
            customerClient,
            insightScheduleInfoPersisterFactory.getObjectPersister(scheduleInfoTableName));
        try
        {
            queueMonitor.processResourcePool(request.getResourcePoolId());
        }
        catch(Throwable t)
        {
            throw new RuntimeException(String.format("Error processing resource pool: %1$s ", request.getResourcePoolId()), t);
        }
    }

    private String getEnvironmentVar(String var) throws BadRequestException
    {
        String value = environmentFacade.getEnv(var);
        if(StringUtils.isBlank(value))
        {
            throw new BadRequestException(String.format("Missing environment var: %1$s", var));
        }
        return value;
    }

    private void logObject(String nodeName, JsonNode node) throws JsonProcessingException
    {
        if(!logger.isDebugEnabled()) return;

        if(node != null)
        {
            logger.debug("[{}]\n{}", nodeName, objectMapper/*.writerWithDefaultPrettyPrinter()*/.writeValueAsString(node));
        }
        else
        {
            logger.debug("[{}] node not found", nodeName);
        }
    }

    private HttpURLConnectionFactory getHttpURLConnectionFactory() throws BadRequestException
    {
        return new AuthHttpURLConnectionFactory();
    }

    public void setEnvironmentFacade(EnvironmentFacade environmentFacade)
    {
        this.environmentFacade = environmentFacade;
    }

    public void setEnvironmentLookupUtils(EnvironmentLookupUtils environmentLookupUtils)
    {
        this.environmentLookupUtils = environmentLookupUtils;
    }

    public void setQueueMonitorFactory(QueueMonitorFactory queueMonitorFactory)
    {
        this.queueMonitorFactory = queueMonitorFactory;
    }
}
