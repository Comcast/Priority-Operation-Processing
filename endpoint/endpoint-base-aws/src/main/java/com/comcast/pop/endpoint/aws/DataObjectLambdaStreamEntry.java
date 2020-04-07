package com.comcast.pop.endpoint.aws;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.object.api.IdentifiedObject;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for Data Object Endpoints on AWS
 * @param <T> The type of object persist/retrieve
 */
public abstract class DataObjectLambdaStreamEntry<T extends IdentifiedObject> extends AbstractLambdaStreamEntry<DataObjectResponse<T>, LambdaDataObjectRequest<T>>
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectPersisterFactory<T> objectPersisterFactory;
    private Class<T> payloadClass;

    // TODO: wrapper class for all the json parsing
    public DataObjectLambdaStreamEntry(Class<T> clazz, ObjectPersisterFactory<T> objectPersisterFactory)
    {
        this.objectPersisterFactory = objectPersisterFactory;
        this.payloadClass = clazz;
    }

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected abstract RequestProcessor<DataObjectResponse<T>, DataObjectRequest<T>> getRequestProcessor(LambdaDataObjectRequest<T> lambdaRequest, ObjectPersister<T> objectPersister);

    protected String getTableEnvironmentVariableName()
    {
        return EnvironmentLookupUtils.DB_TABLE_NAME_ENV_VAR;
    }

    @Override
    public RequestProcessor getRequestProcessor(LambdaDataObjectRequest<T> lambdaRequest)
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
