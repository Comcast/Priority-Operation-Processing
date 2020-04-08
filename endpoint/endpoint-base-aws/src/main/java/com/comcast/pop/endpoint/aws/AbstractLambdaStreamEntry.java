package com.comcast.pop.endpoint.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.api.DefaultServiceResponse;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public abstract class AbstractLambdaStreamEntry<Res extends ServiceResponse, Req extends ServiceRequest> implements JsonRequestStreamHandler
{
    private static final String MDC_CID = "CID";
    private static final String MDC_ENDPOINT_NAME = "ENDPOINT_NAME";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private ResponseWriter responseWriter = new ResponseWriter();

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public abstract RequestProcessor getRequestProcessor(Req serviceRequest);
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
        ErrorResponse errorResponse = null;
        Res responseObject = null;
        // NOTE: this intermediate type is so we can mix Res with types like DefaultServiceResponse for errors
        Object responseBodyObject = null;
        int httpStatusCode = 200;

        try
        {
            final String httpMethod = request.getHTTPMethod("");
            switch (httpMethod)
            {
                case "GET":
                    responseObject = requestProcessor.handleGET(request);
                    if (responseObject == null)
                        httpStatusCode = 404;
                    break;
                case "POST":
                    responseObject = requestProcessor.handlePOST(request);
                    break;
                case "PUT":
                    responseObject = requestProcessor.handlePUT(request);
                    break;
                case "DELETE":
                    responseObject = requestProcessor.handleDELETE(request);
                    break;
                default:
                    // todo: some bad response code
                    httpStatusCode = 405;
                    logger.warn("Unsupported method type.");
            }
            responseObject = createResponseBodyObject(responseObject, request);
            responseBodyObject = responseObject;
        }
        catch (RuntimeServiceException e)
        {
            errorResponse = ErrorResponseFactory.runtimeServiceException(e, getCid());
            logException(e);
        }
        catch (IllegalArgumentException e)
        {
            httpStatusCode = 400;
            errorResponse = ErrorResponseFactory.buildErrorResponse(e, httpStatusCode, getCid());
            logException(e);
        }
        catch(Exception e)
        {
            httpStatusCode = 500;
            errorResponse = ErrorResponseFactory.buildErrorResponse(e, httpStatusCode, getCid());
            logException(e);
        }

        // If there was an error set it up in a DefaultServiceResponse
        if(errorResponse != null)
        {
            DefaultServiceResponse response = new DefaultServiceResponse();
            response.setErrorResponse(errorResponse);
            responseBodyObject = response;
        }

        responseWriter.writeResponse(outputStream, objectMapper, httpStatusCode, responseBodyObject);
    }

    private void logException(Exception e)
    {
        try
        {
            // Don't bother to log the exception as a param, it's broken across newlines and won't have the CID
            logger.error(String.format("Failed to process request. Exception: %1$s %2$s", e.getClass().getSimpleName(), objectMapper.writeValueAsString(e)));
        }
        catch(Exception ex)
        {
            logger.error("Failed to process request.", e);
        }
    }

    /**
     * Creates the response body object to return
     * @param response The object returned by the request processor (may be null)
     * @param request The request object
     * @return The body object to respond with
     */
    protected Res createResponseBodyObject(Res response, Req request)
    {
        if(request != null && response != null)
        {
            response.setCID(request.getCID());
        }
        return response;
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
    protected void setupLoggingMDC(JsonNode rootRequestNode)
    {
        // TODO: the request extractor should probably just be static...
        LambdaRequest lambdaRequest = new LambdaRequest(rootRequestNode);
        String cid = lambdaRequest.getHeader("X-pop-cid");
        MDC.put(MDC_CID, cid == null ? UUID.randomUUID().toString() : cid);
        MDC.put(MDC_ENDPOINT_NAME, lambdaRequest.getEndpoint());
    }

    protected String getCid()
    {
        return MDC.get(MDC_CID);
    }
}
