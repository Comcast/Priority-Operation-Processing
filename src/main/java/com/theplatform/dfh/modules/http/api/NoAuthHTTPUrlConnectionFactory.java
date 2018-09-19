package com.theplatform.dfh.modules.http.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of the HttpURLConnectionFactory that applies no authorization headers
 */
public class NoAuthHTTPUrlConnectionFactory implements HttpURLConnectionFactory
{
    @Override
    public HttpURLConnection getHttpURLConnection(String url) throws IOException
    {
        return getHttpURLConnection(url, null, null);
    }

    @Override
    public HttpURLConnection getHttpURLConnection(String url, String contentType, byte[] postData) throws IOException
    {
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(url).openConnection();
        if(contentType != null)
        {
            httpURLConnection.setRequestProperty("Content-Type", contentType);
        }
        return httpURLConnection;
    }
}
