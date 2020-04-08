package com.comcast.pop.endpoint.client;

import com.comcast.pop.http.api.HttpURLConnectionFactory;

public abstract class POPServiceClient
{
    private final HttpURLConnectionFactory httpUrlConnectionFactory;

    public POPServiceClient(HttpURLConnectionFactory httpURLConnectionFactory)
    {
        this.httpUrlConnectionFactory = httpURLConnectionFactory;
    }

    public HttpURLConnectionFactory getHttpUrlConnectionFactory()
    {
        return httpUrlConnectionFactory;
    }
}
