package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LambdaRequest
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final ObjectMapper staticObjectMapper = new ObjectMapper();

    private ObjectMapper objectMapper = staticObjectMapper;

    protected static final String DEFAULT_PATH_PARAMETER_NAME = "objectid";
    protected static final String BODY_PATH = "/body";
    protected static final String HTTP_METHOD_PATH = "/httpMethod";
    protected static final String STAGE_FIELD_PATH = "/requestContext/stage";
    protected static final String DOMAIN_NAME_FIELD_PATH = "/requestContext/domainName";
    protected static final String STAGE_VARIABLES_PATH = "/stageVariables/";
    protected static final String QUERY_STRING_PARAMETERS_PATH = "/queryStringParameters";
    protected static final String PATH_PARAMETER_PREFIX_PATH = "/pathParameters/";
    protected static final String HEADERS_AUTHORIZATION_PATH = "/headers/Authorization";
    protected static final String HEADERS = "/headers";
    protected static final String HEADER_CID = "X-thePlatform-cid";

    static
    {
        staticObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonNode rootNode;
    private HashMap<String, Object> requestParamMap;

    public LambdaRequest(JsonNode rootNode)
    {
        // this is immediately made available for subclasses
        this.rootNode = rootNode;
        logObject("request: ", rootNode);
        loadRequestParameters();
    }

    public JsonNode getJsonNode()
    {
        return rootNode;
    }

    public HashMap<String, Object> getRequestParamMap()
    {
        return requestParamMap;
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    /**
     * Gets the path parameter name based on the url --
     * https://stackoverflow.com/questions/31329958/how-to-pass-a-querystring-or-route-parameter-to-aws-lambda-from-amazon-api-gatew
     *
     * @return String containing the path parameter name.
     */
    protected String getPathParameterName()
    {
        return DEFAULT_PATH_PARAMETER_NAME;
    }

    protected void loadRequestParameters()
    {
        if (rootNode == null)
            return;

        JsonNode paramNode = rootNode.at(QUERY_STRING_PARAMETERS_PATH);
        if(paramNode.isMissingNode()) return;
        requestParamMap = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> iterator = paramNode.fields();
        while (iterator.hasNext())
        {
            Map.Entry<String, JsonNode> node = iterator.next();
            requestParamMap.put(node.getKey(), node.getValue().asText());
        }
    }

    /**
     * Gets the object id from the path
     * @return The id or null if not found
     */
    public String getIdFromPathParameter()
    {
        return getIdFromPathParameter(DEFAULT_PATH_PARAMETER_NAME);
    }

    /**
     * Gets the object id from the path
     * @param pathParameterName The name of the parameter containing the id to query
     * @return The id or null if not found
     */
    public String getIdFromPathParameter(String pathParameterName)
    {
        String pathParam = PATH_PARAMETER_PREFIX_PATH + pathParameterName;
        String id = getRequestValue(pathParam);
        if(id == null)
        {
            logger.error("No path param found!");
            return null;
        }
        logger.info("objectId: {}", id);
        return id;
    }

    /**
     * Gets the HTTP method of the request or defaults
     * @param defaultValue The default to return if the method cannot be found
     * @return HTTP method or default
     */
    public String getHTTPMethod(String defaultValue)
    {
        String httpMethod = getRequestValue(HTTP_METHOD_PATH);
        return httpMethod == null ? defaultValue : httpMethod;
    }

    public String getStageVariable(String stageVariableName)
    {
        return getRequestValue(STAGE_VARIABLES_PATH + stageVariableName);
    }

    public String getDomainName()
    {
        return getRequestValue(DOMAIN_NAME_FIELD_PATH);
    }

    public String getStageName()
    {
        return getRequestValue(STAGE_FIELD_PATH);
    }

    public String getAuthorizationHeader() { return getRequestValue(HEADERS_AUTHORIZATION_PATH); }

    public String getHeader(String header)
    {
        if(header == null) return null;
        String value = getRequestValue(HEADERS + "/" + header);
        if(value == null) return getRequestValue(HEADERS + "/" + header.toLowerCase());
    }

    public String getCID()
    {
        return getHeader(HEADER_CID);
    }

    /**
     * Gets a string value from the request at the indicated JSON pointer path
     * @param path The JSON pointer path to attempt to read
     * @return The String at the specified path or null if path not found
     */
    public String getRequestValue(String path)
    {
        return getRequestValue(path, null);
    }

    /**
     * Gets a string value from the request at the indicated JSON pointer path
     * @param path The JSON pointer path to attempt to read
     * @return The String at the specified path or defaultValue if path not found
     */
    public String getRequestValue(String path, String defaultValue)
    {
        JsonNode requestValueNode = rootNode.at(path);
        if(requestValueNode.isMissingNode())
        {
            return defaultValue;
        }
        return requestValueNode.asText();
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
