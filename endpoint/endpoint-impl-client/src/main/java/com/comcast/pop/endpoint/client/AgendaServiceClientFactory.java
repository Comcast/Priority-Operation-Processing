package com.comcast.pop.endpoint.client;

import com.comcast.pop.http.api.HttpURLConnectionFactory;

public class AgendaServiceClientFactory
{
    public AgendaServiceClient create(String serviceUrl, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        return new AgendaServiceClient(serviceUrl, httpUrlConnectionFactory);
    }
}
