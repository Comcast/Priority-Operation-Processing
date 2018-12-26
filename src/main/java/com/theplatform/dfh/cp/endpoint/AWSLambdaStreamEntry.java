package com.theplatform.dfh.cp.endpoint;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.endpoint.agenda.aws.AgendaLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.agenda.service.aws.AgendaServiceLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.JsonRequestStreamHandler;
import com.theplatform.dfh.cp.endpoint.aws.LambdaRequest;
import com.theplatform.dfh.cp.endpoint.aws.ResponseWriter;
import com.theplatform.dfh.cp.endpoint.facility.aws.CustomerLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.facility.aws.InsightLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.facility.aws.ResourcePoolLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.OperationProgressLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.progress.aws.ProgressLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.progress.service.aws.ProgressServiceLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.transformrequest.aws.TransformLambdaStreamEntry;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main entry point class for the AWS Endpoint Lambda (will map into another)
 */
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String RESOURCE_PATH_FIELD_PATH = "/requestContext/resourcePath";
    private ResponseWriter responseWriter = new ResponseWriter();

    private static final Map<String, JsonRequestStreamHandler> endpointHandlers = new HashMap<>();

    public AWSLambdaStreamEntry()
    {

    }

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        endpointHandlers.put("/dfh/idm/agenda", new AgendaLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/agenda/service", new AgendaServiceLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/progress/operation", new OperationProgressLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/progress/agenda", new ProgressLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/progress/agenda/service", new ProgressServiceLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/transform", new TransformLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/resourcepool", new ResourcePoolLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/insight", new InsightLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/customer", new CustomerLambdaStreamEntry());
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);

        byte[] inputData = IOUtils.toByteArray(inputStream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputData);
        byteArrayInputStream.mark(Integer.MAX_VALUE);
        JsonNode rootRequestNode = objectMapper.readTree(byteArrayInputStream);
        byteArrayInputStream.reset();
        setupLoggingCid(rootRequestNode);

        logObject("request: ", rootRequestNode);

        JsonNode resourcePathNode = rootRequestNode.at(RESOURCE_PATH_FIELD_PATH);
        if(resourcePathNode.isMissingNode())
        {
            logger.info("Resource path not found.");
            writeResponse(outputStream, 401);
            return;
        }

        String resourcePath = resourcePathNode.asText().replace("/{objectid}", "");

        JsonRequestStreamHandler requestStreamHandler = endpointHandlers.get(resourcePath);
        if(requestStreamHandler != null)
        {
            requestStreamHandler.handleRequest(rootRequestNode, outputStream, context);
        }
        else
        {
            logger.error("[{}] does not map to any endpoint handler.", resourcePath);
            writeResponse(outputStream, 405);
        }
    }

    private void writeResponse(OutputStream outputStream, int httpStatusCode)
    {
        try
        {
            // TODO: write some kind of error object as the body?
            responseWriter.writeResponse(outputStream, objectMapper, httpStatusCode, null);
        }
        catch(IOException e)
        {
            logger.error("Failed to write response to OutputStream.", e);
        }
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

    /**
     * Default CID setup assumes it comes from the CID environment variable. At worst a cid is generated.
     */
    protected void setupLoggingCid(JsonNode rootRequestNode)
    {
        // TODO: the request extractor should probably just be static...
        String cid = new LambdaRequest(rootRequestNode).getHeader("X-thePlatform-cid");
        MDC.put("CID", cid == null ? UUID.randomUUID().toString() : cid);
    }

    public void setResponseWriter(ResponseWriter responseWriter)
    {
        this.responseWriter = responseWriter;
    }
}