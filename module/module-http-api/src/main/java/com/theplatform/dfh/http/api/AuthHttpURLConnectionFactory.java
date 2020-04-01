package com.theplatform.dfh.http.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;

/**
 * Implementation of the connection factory that sets an auth header.
 */
public class AuthHttpURLConnectionFactory extends NoAuthHTTPUrlConnectionFactory
{
    private String authHeaderValue = "dummy-value";
    private String authHeaderName = "Authorization";

    public AuthHttpURLConnectionFactory(String authHeaderName, String authHeaderValue)
    {
        this.authHeaderName = authHeaderName;
        this.authHeaderValue = authHeaderValue;
    }

    public AuthHttpURLConnectionFactory()
    {

    }

    @Override
    public HttpURLConnection getHttpURLConnection(String url, Proxy proxy, String contentType, byte[] postData) throws IOException
    {
        HttpURLConnection httpURLConnection = super.getHttpURLConnection(url, proxy, contentType, postData);
        httpURLConnection.setRequestProperty(authHeaderName, authHeaderValue);
        return httpURLConnection;
    }
}
