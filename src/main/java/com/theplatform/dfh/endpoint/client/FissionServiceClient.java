package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public abstract class FissionServiceClient
{
    private final HttpURLConnectionFactory httpUrlConnectionFactory;

    public FissionServiceClient(HttpURLConnectionFactory httpURLConnectionFactory)
    {
        this.httpUrlConnectionFactory = httpURLConnectionFactory;
    }

    public HttpURLConnectionFactory getHttpUrlConnectionFactory()
    {
        return httpUrlConnectionFactory;
    }
}
