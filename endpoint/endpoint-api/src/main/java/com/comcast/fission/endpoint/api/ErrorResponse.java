package com.comcast.fission.endpoint.api;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 */
public class ErrorResponse
{
    private String title;
    private String description;
    private Integer responseCode;
    private String correlationId;
    private String serverStackTrace;

    public ErrorResponse()
    {

    }


    public ErrorResponse(Throwable e, Integer responseCode, String correlationId)
    {
        if (e != null)
        {
            this.title = e.getClass().getSimpleName();
            this.description = e.getMessage();
            StringWriter stringWriter = new StringWriter();

            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause != rootCause.getCause())
                rootCause = rootCause.getCause();

            rootCause.printStackTrace(new PrintWriter(stringWriter));
            serverStackTrace = stringWriter.toString();
        }

        this.responseCode = responseCode;
        this.correlationId = correlationId;
    }

    public String getTitle()
    {
        return title;
    }

    public ErrorResponse setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public ErrorResponse setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public Integer getResponseCode()
    {
        return responseCode;
    }

    public ErrorResponse setResponseCode(Integer responseCode)
    {
        this.responseCode = responseCode;
        return this;
    }

    public String getCorrelationId()
    {
        return correlationId;
    }

    public ErrorResponse setCorrelationId(String correlationId)
    {
        this.correlationId = correlationId;
        return this;
    }

    public String getServerStackTrace()
    {
        return serverStackTrace;
    }

    public ErrorResponse setServerStackTrace(String serverStackTrace)
    {
        this.serverStackTrace = serverStackTrace;
        return this;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorMessage:: ");
        builder.append(" CID: ");
        builder.append(getCorrelationId());
        builder.append(" ResponseCode: ");
        builder.append(getResponseCode());
        builder.append(" Type: ");
        builder.append(getTitle());
        builder.append(" Message: ");
        builder.append(getDescription());
        builder.append(" Stack Trace: ");
        builder.append(getServerStackTrace());

        return builder.toString();
    }
}
