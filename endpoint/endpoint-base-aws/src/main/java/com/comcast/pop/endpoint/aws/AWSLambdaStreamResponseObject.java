package com.comcast.pop.endpoint.aws;

import java.util.Map;

/**
 * General object for AWS responses on Lambdas using proxy integration / streams
 */
public class AWSLambdaStreamResponseObject
{
    // fields based on: https://aws.amazon.com/premiumsupport/knowledge-center/malformed-502-api-gateway/
    private Boolean isBase64Encoded = false;
    private int statusCode;
    private Map<String, String> headers;
    private String body;


    public AWSLambdaStreamResponseObject()
    {
    }

    public AWSLambdaStreamResponseObject(int statusCode, String body)
    {
        this.statusCode = statusCode;
        this.body = body;
    }

    public Boolean getBase64Encoded()
    {
        return isBase64Encoded;
    }

    public void setBase64Encoded(Boolean base64Encoded)
    {
        isBase64Encoded = base64Encoded;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }
}
