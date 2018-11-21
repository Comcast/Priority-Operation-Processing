package com.theplatform.dfh.cp.endpoint.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.endpoint.api.persistence.DataStore;
import com.theplatform.dfh.cp.endpoint.api.persistence.DataStoreFactory;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public abstract class DataServiceAWSEndpoint<T extends IdentifiedObject> implements RequestStreamHandler
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Class<T> persistenceObjectClazz;
    private final EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private DataStoreFactory<T> dataStoreFactory;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public DataServiceAWSEndpoint(Class<T> clazz, DataStoreFactory<T> dataStoreFactory)
    {
        this.persistenceObjectClazz = clazz;
        this.dataStoreFactory = dataStoreFactory;
    }

    protected abstract BaseRequestProcessor<T> getRequestProcessor(JsonNode rootRequestNode, DataStore<T> objectStore);

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        //Pull out request fields
        //identify method get/put/post
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);
        LambdaRequest<T> request = getRequest(inputStream);

        String tableName = environmentLookupUtils.getTableName(request.getJsonNode());
        logger.info("TableName: {}", tableName);
        DataStore<T> objectStore = dataStoreFactory.getDataStore(tableName);

        BaseRequestProcessor<T> requestProcessor = getRequestProcessor(request.getJsonNode(), objectStore);
        Object responseBodyObject = null;
        int httpStatusCode = 200;

        try
        {
            final String httpMethod = request.getMethod();
            switch (httpMethod)
            {
                case "GET":
                    responseBodyObject = requestProcessor.handleGET(request.getDataObjectId());
                    if (responseBodyObject == null)
                        httpStatusCode = 404;
                    break;
                case "POST":
                    responseBodyObject = requestProcessor.handlePOST(request.getDataObject());
                    break;
                case "PUT":
                     requestProcessor.handlePUT(request.getDataObject());
                    break;
                case "DELETE":
                    requestProcessor.handleDelete(request.getDataObjectId());
                    break;
                default:
                    // todo: some bad response code
                    httpStatusCode = 405;
                    logger.warn("Unsupported method type.");
            }
            responseBodyObject = createResponseBodyObject(responseBodyObject, request.getJsonNode());
        } catch (IllegalArgumentException e)
        {
            httpStatusCode = 400;
            responseBodyObject = e.getMessage();
            // todo maybe make this message json formatted?
        }

        String responseBody = responseBodyObject == null ? null : objectMapper.writeValueAsString(responseBodyObject);
        String response = objectMapper.writeValueAsString(createResponseObject(httpStatusCode, responseBody, request.getJsonNode()));
        logger.info("Response {}", response);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(response);
        writer.close();
    }

    protected LambdaRequest<T> getRequest(InputStream stream) throws BadRequestException
    {
        return new LambdaRequest<>(stream, persistenceObjectClazz);
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
     * Creates the response object to return
     * @param httpStatusCode The http status code to set on the response
     * @param responseBody The body to set on the response
     * @param rootRequestNode The root node of the incoming request
     * @return The object to respond with
     */
    protected AWSLambdaStreamResponseObject createResponseObject(int httpStatusCode, String responseBody, JsonNode rootRequestNode)
    {
        return new AWSLambdaStreamResponseObject(httpStatusCode, responseBody);
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

}

