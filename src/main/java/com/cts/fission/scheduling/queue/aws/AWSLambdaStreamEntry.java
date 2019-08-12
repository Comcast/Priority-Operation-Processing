package com.cts.fission.scheduling.queue.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.cts.fission.scheduling.queue.aws.persistence.PersistentInsightScheduleInfoConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.cts.fission.scheduling.queue.InsightScheduleInfo;
import com.cts.fission.scheduling.queue.monitor.QueueMonitor;
import com.cts.fission.scheduling.queue.monitor.QueueMonitorFactory;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.aws.sqs.AmazonSQSClientFactoryImpl;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersisterFactory;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import com.theplatform.module.authentication.client.EncryptedAuthenticationClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Main entry point class for a CloudWatch Event trigger
 *
 * The incoming request from an event is whatever is specified in the event (assuming constant JSON text)
 */
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String ENV_IDM_ENCRYPTED_PASS = "IDM_ENCRYPTED_PASS";
    private final String ENV_IDM_USER = "IDM_USER";
    private final String ENV_IDENTITY_URL = "IDENTITY_URL";
    private final String ENV_ENDPOINT_URL = "ENDPOINT_URL";
    private final String ENV_RESOURCEPOOL_ENDPOINT_PATH = "RESOURCEPOOL_ENDPOINT_PATH";
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
            new DynamoDBPersisterFactory<>("id", ReadyAgenda.class, null)
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
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);

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
        if(StringUtils.isBlank(request.getResourcePoolId()))
        {
            processAllResourcePools(request, endpointURL);
        }
        else
        {
            processResourcePool(request, endpointURL);
        }
    }

    private void processAllResourcePools(ResourcePoolSchedulerRequest request, String endpointURL)
    {
        String resourcePoolEndpointPath = getEnvironmentVar(ENV_RESOURCEPOOL_ENDPOINT_PATH);
        HttpObjectClient<ResourcePool> resourcePoolClient = new HttpObjectClient<>(
            environmentLookupUtils.getAPIEndpointURL(endpointURL, request.getStageId(), resourcePoolEndpointPath),
            getHttpURLConnectionFactory(),
            ResourcePool.class);

        try
        {
            DataObjectResponse<ResourcePool> response = resourcePoolClient.getObjects(new ArrayList<>());
            if(response.isError())
            {
                logger.error("Failed to get all resource pools!");
                return;
            }
            response.getAll().stream().forEach( resourcePool ->
                {
                    logger.info("Resource Pool to process: {}:{}", resourcePool.getId(), resourcePool.getTitle());
/*
                    AWSLambda awsLambda = AWSLambdaClient.builder()
                        .build();
                    InvokeRequest invokeRequest = new InvokeRequest();
                    invokeRequest.setFunctionName();
                    awsLambda.invoke()*/
                }
            );
        }
        catch (Throwable t)
        {
            throw new RuntimeException("Error processing queue for resource pools.", t);
        }
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
        String identityUrl = getEnvironmentVar(ENV_IDENTITY_URL);
        String user = getEnvironmentVar(ENV_IDM_USER);
        String encryptedPass = environmentLookupUtils.getEncryptedVarFromEnvironment(ENV_IDM_ENCRYPTED_PASS);

        EncryptedAuthenticationClient encryptedAuthenticationClient = new EncryptedAuthenticationClient(identityUrl, user, encryptedPass, null);

        return new IDMHTTPUrlConnectionFactory(encryptedAuthenticationClient);
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
