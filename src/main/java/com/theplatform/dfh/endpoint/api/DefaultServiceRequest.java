package com.theplatform.dfh.endpoint.api;

import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;

import java.util.HashMap;
import java.util.Map;

public class DefaultServiceRequest<T> implements ServiceRequest<T>
{
    private String httpMethod;
    private String endpoint;
    private String authorizationHeader;
    private String cid;
    private T payload;
    private Map<String, String> headers = new HashMap<>();
    private AuthorizationResponse authorizationResponse;

    @Override
    public AuthorizationResponse getAuthorizationResponse()
    {
        return authorizationResponse;
    }

    public void setAuthorizationResponse(AuthorizationResponse authorizationResponse)
    {
        this.authorizationResponse = authorizationResponse;
    }

    public void setHttpMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public void setAuthorizationHeader(String authorizationHeader)
    {
        this.authorizationHeader = authorizationHeader;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
    }

    public void setHeaders(Map<String, String> headers)
    {
        if(headers == null)
        {
            this.headers.clear();
            return;
        }
        this.headers = headers;
    }

    public void setPayload(T payload)
    {
        this.payload = payload;
    }

    @Override
    public String getHTTPMethod(String defaultValue)
    {
        return httpMethod;
    }

    @Override
    public String getEndpoint()
    {
        return endpoint;
    }

    @Override
    public String getAuthorizationHeader()
    {
        return authorizationHeader;
    }

    @Override
    public String getHeader(String header)
    {
        return headers.get(header);
    }

    @Override
    public String getCID()
    {
        return cid;
    }

    @Override
    public T getPayload()
    {
        return payload;
    }
}
