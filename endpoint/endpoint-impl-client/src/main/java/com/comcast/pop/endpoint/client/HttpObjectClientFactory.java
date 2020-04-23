package com.comcast.pop.endpoint.client;

import com.comcast.pop.http.api.HttpURLConnectionFactory;
import com.comcast.pop.object.api.IdentifiedObject;

public class HttpObjectClientFactory
{
    private HttpURLConnectionFactory httpURLConnectionFactory;

    public HttpObjectClientFactory(HttpURLConnectionFactory httpURLConnectionFactory)
    {
        this.httpURLConnectionFactory = httpURLConnectionFactory;
    }

    public <T extends IdentifiedObject> HttpObjectClient<T> createClient(String endpointURL, Class<T> objectClass)
    {
        return new HttpObjectClient<>(endpointURL, httpURLConnectionFactory, objectClass);
    }
}
