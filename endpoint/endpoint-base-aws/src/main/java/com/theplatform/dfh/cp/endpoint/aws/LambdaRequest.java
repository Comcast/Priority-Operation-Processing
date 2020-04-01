package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.fission.endpoint.api.BadRequestException;
import com.comcast.fission.endpoint.api.DefaultServiceRequest;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponse;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponseBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class LambdaRequest<T> extends DefaultServiceRequest<T>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final ObjectMapper staticObjectMapper = new ObjectMapper();

    private ObjectMapper objectMapper = staticObjectMapper;

    protected static final String DEFAULT_PATH_PARAMETER_NAME = "objectid";
    protected static final String BODY_PATH = "/body";
    protected static final String HTTP_METHOD_PATH = "/requestContext/httpMethod";
    protected static final String STAGE_FIELD_PATH = "/requestContext/stage";
    protected static final String REQUEST_PATH = "/requestContext/resourcePath";
    protected static final String DOMAIN_NAME_FIELD_PATH = "/requestContext/domainName";
    protected static final String AUTHORIZATION_RESPONSE = "/requestContext/authorizer";
    protected static final String STAGE_VARIABLES_PATH = "/stageVariables/";
    protected static final String QUERY_STRING_PARAMETERS_PATH = "/queryStringParameters";
    protected static final String PATH_PARAMETER_PREFIX_PATH = "/pathParameters/";
    protected static final String HEADER_AUTHORIZATION = "Authorization";
    protected static final String HEADERS = "/headers";
    protected static final String HEADER_CID = "X-thePlatform-cid";

    static
    {
        staticObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonNode rootNode;
    private HashMap<String, Object> requestParamMap;
    private AuthorizationResponse authorizationResponse;

    public LambdaRequest(JsonNode rootNode, Class<T> requestObjectClass)
    {
        this(rootNode);
        if(rootNode == null) return;
        setPayload(parsePayloadObject(requestObjectClass));
    }
    public LambdaRequest(JsonNode rootNode)
    {
        // this is immediately made available for subclasses
        this.rootNode = rootNode;
        if(rootNode == null) return;

        //logObject("request: ", rootNode);
        parseRequestParameters();
        setCid(parseCID());
        setHttpMethod(parseHTTPMethod("GET"));
        setAuthorizationHeader(parseAuthorizationHeader());
        setEndpoint(parseEndpoint());
        setAuthorizationResponse(parseAuthorizationResponse());
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

    private void parseRequestParameters()
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
            return null;
        }
        logger.debug("path param objectId: {}", id);
        return id;
    }

    /**
     * Gets the HTTP method of the request or defaults
     * @param defaultValue The default to return if the method cannot be found
     * @return HTTP method or default
     */
    private String parseHTTPMethod(String defaultValue)
    {
        return getRequestValue(HTTP_METHOD_PATH, defaultValue);
    }

    public String parseEndpoint()
    {
        return getRequestValue(REQUEST_PATH, null);
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

    private String parseAuthorizationHeader() { return getHeader(HEADER_AUTHORIZATION); }

    /**
     * Gets the header value (attemping with case specified and lower case -- a reality for some reason)
     * @param header The header to retrieve
     * @return The value of the header or null
     */
    @Override
    public String getHeader(String header)
    {
        if(header == null) return null;
        String value = getRequestValue(HEADERS + "/" + header);
        if(value == null) return getRequestValue(HEADERS + "/" + header.toLowerCase());
        return value;
    }

    private String parseCID()
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

    public T parsePayloadObject(Class<T> requestObjectClass) throws BadRequestException
    {
        try
        {
            JsonNode bodyNode = getJsonNode().at(BODY_PATH);
            if(bodyNode.isMissingNode())
            {
                // TODO: further decide how this is handled...
                return null;
            }
            String bodyText = bodyNode.asText();
            if(StringUtils.isBlank(bodyText))
            {
                return null;
            }

            return (getObjectMapper().readValue(bodyText, requestObjectClass));
        }
        catch (IOException e)
        {
            throw new BadRequestException("Request body is not recognized as '" + requestObjectClass.getName() + "'", e);
        }
    }
    /**
     * "authorizer": {
     *             "userName": "me@me.com",
     *             "accounts": "123456",
     *             "userId": "654321",
     *             "isSuperUser": "false"
     *         }
     **/
    public AuthorizationResponse parseAuthorizationResponse()
    {
        JsonNode requestValueNode = rootNode.at(AUTHORIZATION_RESPONSE);
        AuthorizationResponseBuilder builder = new AuthorizationResponseBuilder();
        AuthorizationResponse authorizationResponse;
        if(requestValueNode.isMissingNode())
        {
            authorizationResponse = builder.build();
        }
        else
        {
            builder.withUsername(asText(requestValueNode.at("/userName")));
            builder.withAccounts(asText(requestValueNode.at("/accounts")));
            builder.withUserId(asText(requestValueNode.at("/userId")));
            builder.withSuperUser(asText(requestValueNode.at("/isSuperUser")));
            authorizationResponse = builder.build();
        }
        if(logger.isDebugEnabled()) logger.debug("AuthorizedResponse {}",authorizationResponse.toString());
        return authorizationResponse;
    }
    private String asText(JsonNode jsonNode)
    {
        if(jsonNode == null || jsonNode.isMissingNode()) return null;
        return jsonNode.asText();
    }

    protected void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }
}
