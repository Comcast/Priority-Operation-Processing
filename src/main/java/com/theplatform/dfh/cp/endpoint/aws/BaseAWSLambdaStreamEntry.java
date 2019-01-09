package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for CP Object Endpoints on AWS
 * @param <T> The type of object persist/retrieve
 */
public abstract class BaseAWSLambdaStreamEntry<T extends IdentifiedObject> extends AbstractLambdaStreamEntry<LambdaDataObjectRequest<T>>
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectPersisterFactory<T> objectPersisterFactory;
    private Class<T> payloadClass;

    // TODO: wrapper class for all the json parsing
    public BaseAWSLambdaStreamEntry(Class<T> clazz, ObjectPersisterFactory<T> objectPersisterFactory)
    {
        this.objectPersisterFactory = objectPersisterFactory;
        this.payloadClass = clazz;
    }

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected abstract RequestProcessor getRequestProcessor(LambdaDataObjectRequest<T> lambdaRequest, ObjectPersister<T> objectPersister);

    protected String getTableEnvironmentVariableName()
    {
        return EnvironmentLookupUtils.DB_TABLE_NAME_ENV_VAR;
    }

    @Override
    public RequestProcessor getRequestProcessor(LambdaDataObjectRequest lambdaRequest)
    {
        String tableName = environmentLookupUtils.getTableName(lambdaRequest, getTableEnvironmentVariableName());
        logger.info("TableName: {}", tableName);
        ObjectPersister<T> objectPersister = objectPersisterFactory.getObjectPersister(tableName);

        return getRequestProcessor(lambdaRequest, objectPersister);
    }

    public LambdaDataObjectRequest<T> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaDataObjectRequest<>(node, payloadClass);
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
}
