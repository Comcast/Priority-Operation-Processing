package com.theplatform.dfh.cp.endpoint.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractLambdaStreamEntry<R extends LambdaRequest> implements JsonRequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private ResponseWriter responseWriter = new ResponseWriter();

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public abstract RequestProcessor getRequestProcessor(R lambdaRequest);
    public abstract R getRequest(JsonNode node) throws BadRequestException;

    protected String getTableEnvironmentVariableName()
    {
        return EnvironmentLookupUtils.DB_TABLE_NAME_ENV_VAR;
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        JsonNode rootRequestNode = objectMapper.readTree(inputStream);
        handleRequest(rootRequestNode, outputStream, context);
    }

    public void handleRequest(JsonNode inputStreamNode, OutputStream outputStream, Context context) throws IOException
    {
        R request = getRequest(inputStreamNode);

        RequestProcessor requestProcessor = getRequestProcessor(request);
        Object responseBodyObject = null;
        int httpStatusCode = 200;

        try
        {
            final String httpMethod = request.getHTTPMethod("");
            switch (httpMethod)
            {
                case "GET":
                    responseBodyObject = requestProcessor.handleGET(request);
                    if (responseBodyObject == null)
                        httpStatusCode = 404;
                    break;
                case "POST":
                    responseBodyObject = requestProcessor.handlePOST(request);
                    break;
                case "PUT":
                    responseBodyObject = requestProcessor.handlePUT(request);
                    break;
                case "DELETE":
                    responseBodyObject = requestProcessor.handleDelete(request);
                    break;
                default:
                    // todo: some bad response code
                    httpStatusCode = 405;
                    logger.warn("Unsupported method type.");
            }
            responseBodyObject = createResponseBodyObject(responseBodyObject, request.getJsonNode());
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
     * @param rootRequestNode The root node of the incoming request
     * @return The body object to respond with
     */
    protected Object createResponseBodyObject(Object object, JsonNode rootRequestNode)
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

    public void setResponseWriter(ResponseWriter responseWriter)
    {
        this.responseWriter = responseWriter;
    }
}
