package com.theplatform.dfh.endpoint.client.resourcepool;

import com.theplatform.dfh.endpoint.client.FissionClientException;
import org.apache.commons.lang3.StringUtils;

public class ResourcePoolServiceConfig
{
    private String serviceURL;

    public ResourcePoolServiceConfig(String serviceURL)
    {
        if(StringUtils.isEmpty(serviceURL))
            throw new FissionClientException("No service URL provided.");
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
