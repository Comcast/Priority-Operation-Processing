package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class AgendaServiceClientFactory
{
    public AgendaServiceClient create(String serviceUrl, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        return new AgendaServiceClient(serviceUrl, httpUrlConnectionFactory);
    }
}
