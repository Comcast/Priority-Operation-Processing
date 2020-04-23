package com.comcast.pop.endpoint.client;

import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.endpoint.api.ServiceResponse;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.http.api.HttpURLConnectionFactory;
import com.comcast.pop.http.util.URLRequestPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GenericPOPClient<T extends ServiceResponse, R>
{
    private static final Logger logger = LoggerFactory.getLogger(GenericPOPClient.class);

    private final String requestUrl;
    private final HttpURLConnectionFactory httpUrlConnectionFactory;
    private final Class<T> objectClass;
    private JsonHelper jsonHelper = new JsonHelper();
    private URLRequestPerformer urlRequestPerformer = new URLRequestPerformer();

    public GenericPOPClient(String requestUrl, HttpURLConnectionFactory httpUrlConnectionFactory, Class<T> objectClass)
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
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = httpUrlConnectionFactory.getHttpURLConnection(requestUrl);
            byte[] data = requestObject == null
                          ? null
                          : jsonHelper.getJSONString(requestObject).getBytes();
            urlConnection.setRequestMethod("POST");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                data);
            if (result == null || result.length() == 0)
            {
                return null;
            }
            return jsonHelper.getObjectFromString(result, objectClass);
        }
        catch(IOException e)
        {
            return handleExceptions(e, MDC.get("CID"), urlConnection);
        }
    }

    private T handleExceptions(IOException ioException, String cid, HttpURLConnection urlConnection)
    {
        try {
            int responseCode = 400;
            try
            {
                responseCode = urlConnection.getResponseCode();
            }
            catch (IOException | NullPointerException e2)
            {
                logger.debug("Failed to get responseCode from HttpURLConnection.  Using default.", e2);
            }
            T response = objectClass.newInstance();
            response.setErrorResponse(
                ErrorResponseFactory.buildErrorResponse(new POPClientException(String.format("Failed to get %1$s", objectClass.getSimpleName()), ioException),
                responseCode, cid));
            return response;
        }
        catch (IllegalAccessException | InstantiationException e)
        {
            throw new RuntimeServiceException("foobar", e, 400);
        }
    }
}
