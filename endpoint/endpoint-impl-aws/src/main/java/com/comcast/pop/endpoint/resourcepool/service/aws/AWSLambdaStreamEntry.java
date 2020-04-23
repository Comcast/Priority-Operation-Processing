package com.comcast.pop.endpoint.resourcepool.service.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.JsonRequestStreamHandler;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.version.info.ServiceBuildPropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AWSLambdaStreamEntry extends AbstractLambdaStreamEntry
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String RESOURCE_PATH_FIELD_PATH = "/requestContext/resourcePath";

    private static final Map<String, JsonRequestStreamHandler> endpointHandlers = new HashMap<>();

    public AWSLambdaStreamEntry()
    {
    }

    static
    {
        endpointHandlers.put("/pop/resourcepool/service/getAgenda", new GetAgendaLambdaStreamEntry());
        endpointHandlers.put("/pop/resourcepool/service/createAgenda", new CreateAgendaLambdaStreamEntry());
        endpointHandlers.put("/pop/resourcepool/service/updateAgendaProgress", new UpdateAgendaProgressLambdaStreamEntry());
        endpointHandlers.put("/pop/resourcepool/service/updateAgenda", new UpdateAgendaLambdaStreamEntry());
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger, false);

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

        String resourcePath = resourcePathNode.asText();

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