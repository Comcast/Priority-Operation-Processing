package com.cts.fission.scheduling.monitor.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.cts.fission.scheduling.monitor.QueueMetricMonitor;
import com.cts.fission.scheduling.monitor.QueueMetricMonitorFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.endpoint.client.HttpObjectClientFactory;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;
import com.theplatform.dfh.scheduling.aws.persistence.DynamoDbReadyAgendaPersisterFactory;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Main entry point class for a CloudWatch Event trigger
 *
 * The incoming request from an event is whatever is specified in the event (assuming constant JSON text)
 */
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    public static final TableIndexes READY_AGENDA_TABLE_INDEXES
        = new TableIndexes().withIndex("insightid_index", "insightId");

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ENV_IDM_ENCRYPTED_PASS = "IDM_ENCRYPTED_PASS";
    public static final String ENV_IDM_USER = "IDM_USER";
    public static final String ENV_IDENTITY_URL = "IDENTITY_URL";
    public static final String ENV_ENDPOINT_URL = "ENDPOINT_URL";
    public static final String ENV_RESOURCEPOOL_ENDPOINT_PATH = "RESOURCEPOOL_ENDPOINT_PATH";
    public static final String ENV_INSIGHT_ENDPOINT_PATH = "INSIGHT_ENDPOINT_PATH";
    public static final String ENV_READY_AGENDA_TABLE = "READY_AGENDA_TABLE";

    private final ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;

    private QueueMetricMonitorFactory queueMonitorFactory = new QueueMetricMonitorFactory();
    private EnvironmentFacade environmentFacade = new EnvironmentFacade();
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private MetricReporterFactory metricReporterFactory = new MetricReporterFactory();
    private HttpObjectClientFactory httpObjectClientFactory;

    public AWSLambdaStreamEntry()
    {
        this.readyAgendaPersisterFactory = new DynamoDbReadyAgendaPersisterFactory();
        this.httpObjectClientFactory = new HttpObjectClientFactory(createHttpURLConnectionFactory());
    }

    public AWSLambdaStreamEntry(
        ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory,
        HttpObjectClientFactory httpObjectClientFactory
    )
    {
        this.readyAgendaPersisterFactory = readyAgendaPersisterFactory;
        this.httpObjectClientFactory = httpObjectClientFactory;
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger, false);

        ResourcePoolMonitorRequest request;
        try
        {
            request = objectMapper.readValue(inputStream, ResourcePoolMonitorRequest.class);
        }
        catch(IOException e)
        {
            throw new BadRequestException("Failed to read input as ResourcePoolMonitorRequest.", e);
        }
        if(request == null || request.getStageId() == null)
        {
            throw new BadRequestException("Request must have a stageId.");
        }

        String endpointURL = getEnvironmentVar(ENV_ENDPOINT_URL);
        performMetricAndAlerts(request, endpointURL);
    }

    private void performMetricAndAlerts(ResourcePoolMonitorRequest request, String endpointURL)
    {
        String resourcePoolEndpointPath = getEnvironmentVar(ENV_RESOURCEPOOL_ENDPOINT_PATH);

        String insightEndpointPath = getEnvironmentVar(ENV_INSIGHT_ENDPOINT_PATH);
        String readyAgendaTablePrefix = getEnvironmentVar(ENV_READY_AGENDA_TABLE);

        final String resourcePoolEndpointURL = environmentLookupUtils.getAPIEndpointURL(endpointURL, request.getStageId(), resourcePoolEndpointPath);
        final String insightEndpointURL = environmentLookupUtils.getAPIEndpointURL(endpointURL, request.getStageId(), insightEndpointPath);
        HttpObjectClient<ResourcePool> resourcePoolClient = getHttpObjectClient(ResourcePool.class, resourcePoolEndpointURL);
        HttpObjectClient<Insight> insightClient = getHttpObjectClient(Insight.class, insightEndpointURL);

        final String readyAgendaTableName = environmentLookupUtils.getTableName(readyAgendaTablePrefix, request.getStageId());

        logger.info("ReadyAgenda Table: {}", readyAgendaTableName);
        MetricReporter metricReporter = metricReporterFactory.createInstance(environmentFacade.getEnv());

        QueueMetricMonitor queueMonitor = queueMonitorFactory.createQueueMonitor(
            readyAgendaPersisterFactory.getObjectPersister(readyAgendaTableName),
            insightClient, metricReporter);

        Set<String> failedResourcePoolMetrics = new HashSet<>();
        try
        {
            DataObjectResponse<ResourcePool> response = resourcePoolClient.getObject(request.getResourcePoolId());
            if(response.isError() || response.getFirst() == null)
            {
                logger.error("Failed to get resource pool: {}", request.getResourcePoolId());
                return;
            }
            ResourcePool resourcePool = response.getFirst();
            logger.info("Resource Pool to process: {}:{}", resourcePool.getId(), resourcePool.getTitle());
            try
            {
                queueMonitor.monitor(resourcePool.getId());
            }
            catch(Throwable t)
            {
                logger.error(String.format("Error processing resource pool: %1$s ", resourcePool.getId()), t);
                failedResourcePoolMetrics.add(resourcePool.getId());
            }
        }
        catch (Throwable t)
        {
            throw new RuntimeException(String.format("Error processing resource pool: %1$s.", request.getResourcePoolId()), t);
        }
        if(failedResourcePoolMetrics.size() > 0)
            throw new RuntimeException(String.format("Error processing resource pool: %1$s ", String.join(",", failedResourcePoolMetrics)));
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

    private HttpURLConnectionFactory createHttpURLConnectionFactory() throws BadRequestException
    {
//        String identityUrl = getEnvironmentVar(ENV_IDENTITY_URL);
//        String user = getEnvironmentVar(ENV_IDM_USER);
//        String encryptedPass = environmentLookupUtils.getEncryptedVarFromEnvironment(ENV_IDM_ENCRYPTED_PASS);
//
//        EncryptedAuthenticationClient encryptedAuthenticationClient = new EncryptedAuthenticationClient(identityUrl, user, encryptedPass, null);

        return new NoAuthHTTPUrlConnectionFactory();

    }

    private <T extends IdentifiedObject> HttpObjectClient<T> getHttpObjectClient(Class<T> objectClass, String endpointURL) throws BadRequestException
    {
        return httpObjectClientFactory.createClient(endpointURL, objectClass);
    }

    public void setEnvironmentFacade(EnvironmentFacade environmentFacade)
    {
        this.environmentFacade = environmentFacade;
    }

    public void setEnvironmentLookupUtils(EnvironmentLookupUtils environmentLookupUtils)
    {
        this.environmentLookupUtils = environmentLookupUtils;
    }

    public void setQueueMonitorFactory(QueueMetricMonitorFactory queueMonitorFactory)
    {
        this.queueMonitorFactory = queueMonitorFactory;
    }

    public void setHttpObjectClientFactory(HttpObjectClientFactory httpObjectClientFactory)
    {
        this.httpObjectClientFactory = httpObjectClientFactory;
    }
}
