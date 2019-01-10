package com.theplatform.dfh.cp.endpoint.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public abstract class AbstractLambdaStreamEntry<Res extends ServiceResponse, Req extends ServiceRequest> implements JsonRequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private ResponseWriter responseWriter = new ResponseWriter();

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public abstract RequestProcessor getRequestProcessor(Req lambdaRequest);
    public abstract Req getRequest(JsonNode node) throws BadRequestException;

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        JsonNode rootRequestNode = objectMapper.readTree(inputStream);
        handleRequest(rootRequestNode, outputStream, context);
    }

    public void handleRequest(JsonNode inputStreamNode, OutputStream outputStream, Context context) throws IOException
    {
        Req request = getRequest(inputStreamNode);

        RequestProcessor<Res, Req> requestProcessor = getRequestProcessor(request);
        Object responseBodyObject = null;
        int httpStatusCode = 200;

        try
        {
            final String httpMethod = request.getHTTPMethod("");
            switch (httpMethod)
            {
                case "GET":
                    requestProcessor.getRequestValidator().validateGET(request);
                    responseBodyObject = requestProcessor.handleGET(request);
                    if (responseBodyObject == null)
                        httpStatusCode = 404;
                    break;
                case "POST":
                    requestProcessor.getRequestValidator().validatePOST(request);
                    responseBodyObject = requestProcessor.handlePOST(request);
                    break;
                case "PUT":
                    requestProcessor.getRequestValidator().validatePUT(request);
                    responseBodyObject = requestProcessor.handlePUT(request);
                    break;
                case "DELETE":
                    requestProcessor.getRequestValidator().validateDELETE(request);
                    responseBodyObject = requestProcessor.handleDELETE(request);
                    break;
                default:
                    // todo: some bad response code
                    httpStatusCode = 405;
                    logger.warn("Unsupported method type.");
            }
            responseBodyObject = createResponseBodyObject(responseBodyObject, request);
        }
        catch (IllegalArgumentException e)
        {
            httpStatusCode = 400;
            responseBodyObject = e.getMessage();
            // todo maybe make this message json formatted?
        }
        catch(Exception e)
        {
            httpStatusCode = 500;
            responseBodyObject = e.getMessage();
            try
            {
                // Don't bother to log the exception as a param, it's broken across newlines and won't have the CID
                logger.error(String.format("Failed to process request. Exception: %1$s", objectMapper.writeValueAsString(e)));
            }
            catch(Exception ex)
            {
                logger.error("Failed to process request.", e);
            }
        }

        responseWriter.writeResponse(outputStream, objectMapper, httpStatusCode, responseBodyObject);
    }

    /**
     * Creates the response body object to return
     * @param object The object returned by the request processor (may be null)
     * @param request The request object
     * @return The body object to respond with
     */
    protected Object createResponseBodyObject(Object object, Req request)
    {
        return object;
    }

    /**
     * Gets the entry from the given json node defaulting if not found
     * @param rootRequestNode The root node to search in
     * @param jsonPtrExpr The json pointer string to use
     * @param defaultValue The default value if the node is missing
     * @return The value at the specified pointer or the default value
     */
    protected String getRequestEntry(JsonNode rootRequestNode, String jsonPtrExpr, String defaultValue)
    {
        JsonNode node = rootRequestNode.at(jsonPtrExpr);
        if(node.isMissingNode()) return null;
        return node.asText(defaultValue);
    }

    public static ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    public ResponseWriter getResponseWriter()
    {
        return responseWriter;
    }

    public void setResponseWriter(ResponseWriter responseWriter)
    {
        this.responseWriter = responseWriter;
    }


    public void logObject(String nodeName, JsonNode node) throws JsonProcessingException
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
}
