package com.theplatform.dfh.endpoint.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Http specific implementation of the CPObjectClient
 * @param <T>
 */
public class HttpObjectClient<T extends IdentifiedObject> implements ObjectClient<T>
{
    private static final Logger logger = LoggerFactory.getLogger(HttpObjectClient.class);

    public final static String DEFAULT_RESPONSE_MESSAGE = "[No message from service provided]";
    private final String endpointURL;
    private final HttpURLConnectionFactory httpUrlConnectionFactory;
    private final Class<T> objectClass;
    private JsonHelper jsonHelper = new JsonHelper();
    private URLRequestPerformer urlRequestPerformer = new URLRequestPerformer();
    
    public HttpObjectClient(String endpointURL, HttpURLConnectionFactory httpUrlConnectionFactory, Class<T> clazz)
    {
        this.endpointURL = endpointURL;
        this.httpUrlConnectionFactory = httpUrlConnectionFactory;
        this.objectClass = clazz;
    }

    public DataObjectResponse<T> getObjects(String queryParams)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + queryParams);
            urlConnection.setRequestMethod("GET");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                null);
            logger.info("Object: {}", result);
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return null;
            }
            return parseDataObjectResponse(result);
        }
        catch(IOException e)
        {
            return buildExceptionResponse(e, String.format("Failed to perform retrieve for query %1$s: %2$s", objectClass.getSimpleName(), queryParams), urlConnection);
        }
    }

    public DataObjectResponse<T> getObjects(List<Query> queries)
    {
        return getObjects(getQueryParams(queries));
    }

    public DataObjectResponse<T> getObject(String id)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + "/" + getURLEncodedId(id));
            urlConnection.setRequestMethod("GET");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                null);
            logger.info("Object: {}", result);
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return null;
            }
            return parseDataObjectResponse(result);
        }
        catch(IOException e)
        {
            return buildExceptionResponse(e, String.format("Failed to perform retrieve for %1$s: %2$s", objectClass.getSimpleName(), id), urlConnection);
        }
    }

    public DataObjectResponse<T> persistObject(T object)
    {
        byte[] postData = jsonHelper.getJSONString(object).getBytes();
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL, "application/json", postData);
            urlConnection.setRequestMethod("POST");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                postData);
            return parseDataObjectResponse(result);
        }
        catch(IOException e)
        {
            return buildExceptionResponse(e, String.format("Failed to perform persist for %1$s", objectClass.getSimpleName()), urlConnection);
        }
    }

    public DataObjectResponse<T> updateObject(T object, String id)
    {
        byte[] postData = jsonHelper.getJSONString(object).getBytes();
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + "/" + getURLEncodedId(id), "application/json", postData);
            urlConnection.setRequestMethod("PUT");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                postData);
            return parseDataObjectResponse(result);

        }
        catch(IOException e)
        {
            return buildExceptionResponse(e, String.format("Failed to perform update for %1$s", objectClass.getSimpleName()), urlConnection);
        }
    }

    public DataObjectResponse<T> deleteObject(String id)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + "/" + getURLEncodedId(id));
            urlConnection.setRequestMethod("DELETE");
            String result = urlRequestPerformer.performURLRequest(urlConnection, null);
            return parseDataObjectResponse(result);
        }
        catch (IOException e)
        {
            return buildExceptionResponse(e, String.format("Failed to perform delete for %1$s:%2$s", objectClass.getSimpleName(), id), urlConnection);
        }
    }

    public Class<T> getObjectClass()
    {
        return objectClass;
    }

    protected String getURLEncodedId(String id)
    {
        try
        {
            return URLEncoder.encode(id, StandardCharsets.UTF_8.name());
        }
        catch(UnsupportedEncodingException e)
        {
            logger.error(String.format("%1$s encoding is not supported. The world is ending.", StandardCharsets.UTF_8.name()), e);
        }
        return id;
    }

    protected String getQueryParams(Collection<Query> queries)
    {
        if(queries == null || queries.size() == 0) return "";
        List<String> params = new LinkedList<>();
        for (Query query : queries)
        {
            params.add(query.toQueryParam());
        }
        return "?" + String.join("&", params);
    }

    public void setUrlRequestPerformer(URLRequestPerformer urlRequestPerformer)
    {
        this.urlRequestPerformer = urlRequestPerformer;
    }

    private DataObjectResponse<T> parseDataObjectResponse(String response) throws IOException
    {
        ObjectMapper objectMapper = jsonHelper.getObjectMapper();
        JavaType type = jsonHelper.getObjectMapper().getTypeFactory().constructParametricType(DefaultDataObjectResponse.class, objectClass);
        return objectMapper.readValue(response, type);
    }

    protected DataObjectResponse<T> buildExceptionResponse(IOException e, String message, HttpURLConnection urlConnection)
    {
        String responseMessage = null;
        int responseCode = 400;
        try
        {
            responseMessage = urlConnection.getResponseMessage();
            responseCode = urlConnection.getResponseCode();
        }
        catch (IOException | NullPointerException e2)
        {
            logger.debug("Failed to get responseCode from HttpURLConnection.  Using default.", e2);
        }

        if(responseMessage == null)
            responseMessage = DEFAULT_RESPONSE_MESSAGE;

        return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(
            new ObjectClientException(String.join(" :: ", responseMessage, message), e),
            responseCode,
            MDC.get("CID")));
    }
}
