package com.theplatform.dfh.endpoint.api;

public interface ServiceRequest<T>
{
    /**
     * Gets the HTTP method of the request or defaults
     * @param defaultValue The default to return if the method cannot be found
     * @return HTTP method or default
     */
    String getHTTPMethod(String defaultValue);

    String getEndpoint();

    String getAuthorizationHeader();

    String getHeader(String header);

    String getCID();

    T getPayload();

}
