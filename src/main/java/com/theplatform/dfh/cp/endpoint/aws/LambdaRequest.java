package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.BadRequestException;
import com.theplatform.dfh.persistence.api.query.Query;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class LambdaRequest<T extends IdentifiedObject>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final String DEFAULT_PATH_PARAMETER_NAME = "objectid";
    private JsonNode rootNode;
    private Class dataObjectClass;
    private HashMap<String, Object> requestParamMap;
    private List<Query> queries;

    public LambdaRequest(JsonNode rootNode, Class dataObjectClass)
    {
        // this is immediately made available for subclasses
        this.rootNode = rootNode;
        logObject("request: ", rootNode);
        loadQueryParameters();
        this.dataObjectClass = dataObjectClass;
    }

    protected String getMethod()
    {
        JsonNode httpMethodNode = rootNode.at("/httpMethod");
        if (httpMethodNode.isMissingNode())
        {
            logger.info("Method not found!");
        }
        return httpMethodNode.asText("UNKNOWN").toUpperCase();
    }

    protected T getDataObject() throws BadRequestException
    {
        try
        {
            return (T) objectMapper.readValue(StringEscapeUtils.unescapeJson(rootNode.at("/body").asText()), dataObjectClass);
        }
        catch (IOException e)
        {
            throw new BadRequestException("Request body is not recognized as '" + dataObjectClass.getName() + "'", e);
        }
    }

    /**
     * Gets the path parameter name based on the url -- https://stackoverflow
     * .com/questions/31329958/how-to-pass-a-querystring-or-route-parameter-to-aws-lambda-from-amazon-api-gatew
     *
     * @return String containing the path parameter name.
     */
    protected String getPathParameterName()
    {
        return DEFAULT_PATH_PARAMETER_NAME;
    }

    protected String getDataObjectId() throws BadRequestException
    {
        //first see if it's on the path parameter.
        String dataObjectId = getIdFromPathParameter();
        if (dataObjectId != null)
            return dataObjectId;

        //get the Id off the request parameters
        dataObjectId = (String) requestParamMap.get("id");
        if (dataObjectId != null)
            return dataObjectId;

        return getDataObject().getId();
    }

    public JsonNode getJsonNode()
    {
        return rootNode;
    }

    private void loadQueryParameters()
    {
        if (rootNode == null)
            return;

        JsonNode paramNode = rootNode.get("queryStringParameters");
        Iterator<Map.Entry<String, JsonNode>> iterator = paramNode.fields();

        requestParamMap = new HashMap<>();
        queries = new ArrayList<>();
        while (iterator.hasNext())
        {
            Map.Entry<String, JsonNode> node = iterator.next();
            String value = node.getValue().textValue();
            requestParamMap.put(node.getKey(), value);
            final int byIndexLoc = node.getKey().indexOf("by");
            if (byIndexLoc == 0)
            {
                final String field = node.getKey().substring(byIndexLoc);
                queries.add(new Query(field, value));
            }
        }
    }


    public List<Query> getQueries()
    {
        return queries;
    }
    /**
     * Gets the object id from the path
     * @return The id or null if not found
     */
    protected String getIdFromPathParameter()
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
    private void logObject(String nodeName, JsonNode node)
    {
        if(!logger.isDebugEnabled()) return;

        try
        {
            if (node != null)
            {
                logger.debug("[{}]\n{}", nodeName, objectMapper/*.writerWithDefaultPrettyPrinter()*/.writeValueAsString(node));
            }
            else
            {
                logger.debug("[{}] node not found", nodeName);
            }
        }
        catch (JsonProcessingException e)
        {
            //ignore our bad logging
            logger.error("Unable to log JsonNode '{}'", nodeName, e);
        }
    }
}
