package com.comcast.pop.endpoint.client.resourcepool;

import com.comcast.pop.endpoint.client.POPClientException;
import org.apache.commons.lang3.StringUtils;

public class ResourcePoolServiceConfig
{
    private String serviceURL;

    public ResourcePoolServiceConfig(String serviceURL)
    {
        if(StringUtils.isEmpty(serviceURL))
            throw new POPClientException("No service URL provided.");
        if(serviceURL.endsWith("/"))
        {
            this.serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
        }
        else
        {
            this.serviceURL = serviceURL;
        }
    }

    public String getProviderUrl(ResourcePoolEndpoint method)
    {
        return serviceURL + "/" + method.getPath();
    }
}
