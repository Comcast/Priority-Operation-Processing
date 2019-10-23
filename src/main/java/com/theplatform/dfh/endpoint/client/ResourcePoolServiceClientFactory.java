package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class ResourcePoolServiceClientFactory
{
    public ResourcePoolServiceClient create(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        return new ResourcePoolServiceClient(httpUrlConnectionFactory);
    }

    public ResourcePoolServiceClient create(HttpURLConnectionFactory httpUrlConnectionFactory, String agendaProviderUrl)
    {
        return new ResourcePoolServiceClient(httpUrlConnectionFactory, agendaProviderUrl);
    }
}
