package com.theplatform.dfh.cp.endpoint;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.endpoint.agenda.aws.AgendaLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.agenda.service.aws.ReigniteAgendaLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.agenda.service.aws.IgniteAgendaLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.agendatemplate.aws.AgendaTemplateLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.AbstractLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.JsonRequestStreamHandler;
import com.theplatform.dfh.cp.endpoint.aws.LambdaRequest;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.service.aws.ProgressServiceLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.CustomerLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.InsightLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.resourcepool.aws.ResourcePoolLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.OperationProgressLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.progress.aws.ProgressLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.transformrequest.aws.TransformLambdaStreamEntry;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point class for the AWS Endpoint Lambda (will map into another)
 */
public class AWSLambdaStreamEntry extends AbstractLambdaStreamEntry
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String RESOURCE_PATH_FIELD_PATH = "/requestContext/resourcePath";

    private static final Map<String, JsonRequestStreamHandler> endpointHandlers = new HashMap<>();

    public AWSLambdaStreamEntry()
    {
    }

    static
    {
        endpointHandlers.put("/dfh/idm/agendatemplate", new AgendaTemplateLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/agenda", new AgendaLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/progress/operation", new OperationProgressLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/progress/agenda", new ProgressLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/progress/agenda/service", new ProgressServiceLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/transform", new TransformLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/resourcepool", new ResourcePoolLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/insight", new InsightLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/customer", new CustomerLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/agenda/service/ignite", new IgniteAgendaLambdaStreamEntry());
        endpointHandlers.put("/dfh/idm/agenda/service/reignite", new ReigniteAgendaLambdaStreamEntry());
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);

        byte[] inputData = IOUtils.toByteArray(inputStream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputData);
        byteArrayInputStream.mark(Integer.MAX_VALUE);
        JsonNode rootRequestNode = getObjectMapper().readTree(byteArrayInputStream);
        byteArrayInputStream.reset();
        setupLoggingMDC(rootRequestNode);

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
            getResponseWriter().writeResponse(outputStream, getObjectMapper(), httpStatusCode, null);
        }
        catch(IOException e)
        {
            logger.error("Failed to write response to OutputStream.", e);
        }
    }

    @Override
    public RequestProcessor getRequestProcessor(ServiceRequest lambdaRequest)
    {
        return null;
    }

    @Override
    public LambdaRequest getRequest(JsonNode node) throws BadRequestException
    {
        return null;      //not used
    }
}