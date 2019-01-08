package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GenericFissionClient<T, R>
{
    private static final Logger logger = LoggerFactory.getLogger(GenericFissionClient.class);

    private final String requestUrl;
    private final HttpURLConnectionFactory httpUrlConnectionFactory;
    private final Class<T> objectClass;
    private JsonHelper jsonHelper = new JsonHelper();
    private URLRequestPerformer urlRequestPerformer = new URLRequestPerformer();

    public GenericFissionClient(String requestUrl, HttpURLConnectionFactory httpUrlConnectionFactory, Class<T> objectClass)
    {
        this.requestUrl = requestUrl;
        this.httpUrlConnectionFactory = httpUrlConnectionFactory;
        this.objectClass = objectClass;
    }

    /**
     * Generic POST with a response object returned if able to unmarshall
     * @param requestObject The object to POST
     * @return The response object or null if the result is empty or a 404 http status is returned.
     */
    public T getObjectFromPOST(R requestObject)
    {
        try
        {
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(requestUrl);
            byte[] data = requestObject == null
                          ? null
                          : jsonHelper.getJSONString(requestObject).getBytes();
            urlConnection.setRequestMethod("POST");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                data);
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return null;
            }
            if (result == null || result.length() == 0)
            {
                return null;
            }
            return jsonHelper.getObjectFromString(result, objectClass);
        }
        catch(IOException e)
        {
            throw new FissionClientException(String.format("Failed to get %1$s", objectClass.getSimpleName()), e);
        }
    }
}
