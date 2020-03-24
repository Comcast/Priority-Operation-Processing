package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.client.resourcepool.ResourcePoolServiceConfig;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class ResourcePoolServiceClientFactory
{
    public ResourcePoolServiceClient create(ResourcePoolServiceConfig resourcePoolServiceConfig, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        return new ResourcePoolServiceClient(resourcePoolServiceConfig, httpUrlConnectionFactory);
    }

    public ResourcePoolServiceClient create(String serviceUrl, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        return new ResourcePoolServiceClient(serviceUrl, httpUrlConnectionFactory);
    }
}
