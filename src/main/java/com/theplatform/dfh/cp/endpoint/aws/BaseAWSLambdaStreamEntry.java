package com.theplatform.dfh.cp.endpoint.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.schedule.persistence.api.ObjectPersister;
import com.theplatform.dfh.schedule.status.version.ServiceBuildPropertiesContainer;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *

 // research how we do /agenda/{id} instead of a query param
 * @param <T> The type of object to persist/retrieve
 */

/**
 * Base for CP Object Endpoints on AWS
 * @param <T> The type of object persist/retrieve
 */
public abstract class BaseAWSLambdaStreamEntry<T> implements RequestStreamHandler
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_PATH_PARAMETER_NAME = "objectid";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Class persistenceObjectClazz;
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

    protected  abstract BaseRequestProcessor<T> getRequestProcessor(ObjectPersister<T> objectPersister);
    
    /**
     * Gets the path parameter name based on the url -- https://stackoverflow.com/questions/31329958/how-to-pass-a-querystring-or-route-parameter-to-aws-lambda-from-amazon-api-gatew
     * @return String containing the path parameter name.
     */
    protected String getPathParameterName()
    {
        return DEFAULT_PATH_PARAMETER_NAME;
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);

        JsonNode rootNode = objectMapper.readTree(inputStream);

        logObject("request: ", rootNode);

        JsonNode httpMethodNode = rootNode.at("/httpMethod");
        if(httpMethodNode.isMissingNode())
        {
            logger.info("Method not found!");
        }

        ObjectPersister<T> objectPersister = objectPersisterFactory.getObjectPersister();

        BaseRequestProcessor<T> requestProcessor = getRequestProcessor(objectPersister);
        Object responseObject = null;
        int httpStatusCode = 200;
        switch (httpMethodNode.asText("UNKNOWN").toUpperCase())
        {
            case "GET":
                responseObject = requestProcessor.handleGET(getIdFromPathParameter(rootNode));
                if(responseObject == null) httpStatusCode = 404;
                break;
            case "POST":
                String bodyJson = StringEscapeUtils.unescapeJson(rootNode.at("/body").asText());
                responseObject = requestProcessor.handlePOST((T)objectMapper.readValue(bodyJson, persistenceObjectClazz));
                break;
            case "DELETE":
                requestProcessor.handleDelete(getIdFromPathParameter(rootNode));
                break;
            default:
                // todo: some bad response code
                httpStatusCode = 405;
                logger.warn("Unsupported method type.");
        }
        String responseBody = responseObject == null ? null : objectMapper.writeValueAsString(responseObject);
        logger.info("Response Body: {}", responseBody);
        String response = objectMapper.writeValueAsString(new AWSLambdaStreamResponseObject(httpStatusCode,responseBody));
        logger.info("Response {}", response);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(response);
        writer.close();
    }

    public String getIdFromPathParameter(JsonNode rootNode)
    {
        String pathParam = "/pathParameters/" + getPathParameterName();
        logger.info("Path Param: {}", pathParam);
        JsonNode idPathParameter = rootNode.at(pathParam);
        if(!idPathParameter.isMissingNode())
        {
            logger.info("objectId: {}", idPathParameter.asText());
            return idPathParameter.asText();
        }
        else
        {
            logger.error("No path param found!");
        }
        return null;
    }

    private void logObject(String nodeName, JsonNode node) throws JsonProcessingException
    {
        if(!logger.isDebugEnabled()) return;

        if(node != null)
        {
            logger.debug("[{}]\n{}", nodeName, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
        }
        else
        {
            logger.debug("[{}] node not found", nodeName);
        }
    }
}
