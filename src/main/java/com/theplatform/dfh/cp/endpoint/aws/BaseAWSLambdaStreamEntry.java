package com.theplatform.dfh.cp.endpoint.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Base for CP Object Endpoints on AWS
 * @param <T> The type of object persist/retrieve
 */
public abstract class BaseAWSLambdaStreamEntry<T extends IdentifiedObject> implements JsonRequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Class<T> persistenceObjectClazz;
    protected final EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private ObjectPersisterFactory<T> objectPersisterFactory;

    // TODO: wrapper class for all the json parsing

    public BaseAWSLambdaStreamEntry(Class<T> clazz, ObjectPersisterFactory<T> objectPersisterFactory)
    {
        this.persistenceObjectClazz = clazz;
        this.objectPersisterFactory = objectPersisterFactory;
    }

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected abstract BaseRequestProcessor<T> getRequestProcessor(JsonNode rootRequestNode, ObjectPersister<T> objectPersister);

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
        LambdaRequest<T> request = getRequest(inputStreamNode);

        String tableName = environmentLookupUtils.getTableName(inputStreamNode);
        logger.info("TableName: {}", tableName);
        ObjectPersister<T> objectPersister = objectPersisterFactory.getObjectPersister(tableName);

        BaseRequestProcessor<T> requestProcessor = getRequestProcessor(request.getJsonNode(), objectPersister);
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

    protected LambdaRequest<T> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, persistenceObjectClazz);
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
