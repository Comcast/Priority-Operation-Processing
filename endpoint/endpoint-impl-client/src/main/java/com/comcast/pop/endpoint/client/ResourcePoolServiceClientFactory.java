package com.comcast.pop.endpoint.client;

import com.comcast.pop.endpoint.client.resourcepool.ResourcePoolServiceConfig;
import com.comcast.pop.http.api.HttpURLConnectionFactory;

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
