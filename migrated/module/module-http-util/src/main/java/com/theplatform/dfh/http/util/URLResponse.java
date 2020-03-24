package com.theplatform.dfh.http.util;

import java.util.List;
import java.util.Map;

public class URLResponse
{
    private int statusCode;
    private boolean isError;
    private Exception exception;
    private String responseBody;
    private Map<String, List<String>> headers;

    public Map<String, List<String>> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers)
    {
        this.headers = headers;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public boolean isError()
    {
        return isError;
    }

    public void setError(boolean error)
    {
        isError = error;
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException(Exception exception)
    {
        this.exception = exception;
    }

    public String getResponseBody()
    {
        return responseBody;
    }

    public void setResponseBody(String responseBody)
    {
        this.responseBody = responseBody;
    }
}
