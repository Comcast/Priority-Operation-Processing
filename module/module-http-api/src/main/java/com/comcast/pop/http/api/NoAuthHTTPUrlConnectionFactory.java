package com.comcast.pop.http.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Implementation of the HttpURLConnectionFactory that applies no authorization headers
 */
public class NoAuthHTTPUrlConnectionFactory implements HttpURLConnectionFactory
{
    private static Logger logger = LoggerFactory.getLogger(NoAuthHTTPUrlConnectionFactory.class);

    @Override
    public HttpURLConnection getHttpURLConnection(String url) throws IOException
    {
        return getHttpURLConnection(url, null, null, null);
    }

    @Override
    public HttpURLConnection getHttpURLConnection(String url, String contentType, byte[] postData) throws IOException
    {
        return getHttpURLConnection(url, null, contentType, postData);
    }

    @Override
    public HttpURLConnection getHttpURLConnection(String url, Proxy proxy, String contentType, byte[] postData) throws IOException
    {
        logger.debug("getHttpURLConnection - Content-type: " + contentType);

        HttpURLConnection httpURLConnection;
        if (proxy != null)
        {
            logger.debug("Connecting using proxy " + proxy.toString());
            httpURLConnection = (HttpURLConnection)new URL(url).openConnection(proxy);
        }
        else
        {
            logger.debug("Connecting without proxy");
            httpURLConnection = (HttpURLConnection)new URL(url).openConnection();
        }

        if(contentType != null)
        {
            httpURLConnection.setRequestProperty("Content-Type", contentType);
        }
        logger.debug("getHttpURLConnection - returning connection");
        return httpURLConnection;
    }
}
