package com.theplatform.dfh.endpoint.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class HttpObjectClient<T> implements ObjectClient<T>
{
    private static final Logger logger = LoggerFactory.getLogger(HttpObjectClient.class);

    private final String endpointURL;
    private final HttpURLConnectionFactory httpUrlConnectionFactory;
    private final Class<T> objectClass;
    private JsonHelper jsonHelper = new JsonHelper();
    private URLRequestPerformer urlRequestPerformer = new URLRequestPerformer();
    
    public HttpObjectClient(String endpointURL, HttpURLConnectionFactory httpUrlConnectionFactory, Class clazz)
    {
        this.endpointURL = endpointURL;
        this.httpUrlConnectionFactory = httpUrlConnectionFactory;
        this.objectClass = clazz;
    }

    public DataObjectFeed<T> getObjects(String queryParams)
    {
        try
        {
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + queryParams);
            urlConnection.setRequestMethod("GET");
            String otherResult = urlRequestPerformer.performURLRequest(
                urlConnection,
                null);
            logger.info("Object: {}", otherResult);
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return null;
            }
            ObjectMapper objectMapper = jsonHelper.getObjectMapper();
            JavaType type = objectMapper.getTypeFactory().constructParametricType(DataObjectFeed.class, objectClass);
            return objectMapper.readValue(otherResult, type);
        }
        catch(IOException e)
        {
            throw new ObjectClientException(String.format("Failed to perform retrieve for query %1$s: %2$s", objectClass.getSimpleName(), queryParams), e);
        }
    }

    public DataObjectFeed<T> getObjects(List<Query> queries)
    {
        return getObjects(getQueryParams(queries));
    }

    public T getObject(String id)
    {
        try
        {
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + "/" + getURLEncodedId(id));
            urlConnection.setRequestMethod("GET");
            String otherResult = urlRequestPerformer.performURLRequest(
                urlConnection,
                null);
            logger.info("Object: {}", otherResult);
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return null;
            }
            return jsonHelper.getObjectFromString(otherResult, objectClass);
        }
        catch(IOException e)
        {
            throw new ObjectClientException(String.format("Failed to perform retrieve for %1$s: %2$s", objectClass.getSimpleName(), id), e);
        }
    }

    public ObjectPersistResponse persistObject(T object)
    {
        byte[] postData = jsonHelper.getJSONString(object).getBytes();
        try
        {
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL, "application/json", postData);
            urlConnection.setRequestMethod("POST");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                postData);
            return jsonHelper.getObjectFromString(result, ObjectPersistResponse.class);
        }
        catch(IOException e)
        {
            throw new ObjectClientException(String.format("Failed to perform persist for %1$s", objectClass.getSimpleName()), e);
        }
    }

    public void updateObject(T object, String id)
    {
        byte[] postData = jsonHelper.getJSONString(object).getBytes();
        try
        {
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + "/" + getURLEncodedId(id), "application/json", postData);
            urlConnection.setRequestMethod("PUT");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                postData);
        }
        catch(IOException e)
        {
            throw new ObjectClientException(String.format("Failed to perform update for %1$s", objectClass.getSimpleName()), e);
        }
    }

    public void deleteObject(String id)
    {
        try
        {
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(endpointURL + "/" + getURLEncodedId(id));
            urlConnection.setRequestMethod("DELETE");
            urlRequestPerformer.performURLRequest(urlConnection, null);
        }
        catch (IOException e)
        {
            throw new ObjectClientException(String.format("Failed to perform delete for %1$s:%2$s", objectClass.getSimpleName(), id), e);
        }
    }

    public Class getObjectClass()
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
}
