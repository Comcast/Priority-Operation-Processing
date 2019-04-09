package com.theplatform.dfh.cp.api.progress;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class DiagnosticEvent
{
    private String message;
    private Date time;
    private String stackTrace;
    private Object payload;

    public DiagnosticEvent()
    {

    }

    public DiagnosticEvent(String message)
    {
        this(message, new Date(), null, null);
    }

    public DiagnosticEvent(String message, Exception exception)
    {
        this(message, exception, null);
    }

    public DiagnosticEvent(String message, Exception exception, Object payload)
    {
        this(message, new Date(), convertExceptionToStackTrace(exception), payload);
    }

    public DiagnosticEvent(String message, String stackTrace)
    {
        this(message, stackTrace, null);
    }

    public DiagnosticEvent(String message, String stackTrace, Object payload)
    {
        this(message, new Date(), stackTrace, payload);
    }

    public DiagnosticEvent(String message, Date time, String stackTrace, Object payload)
    {
        this.message = message;
        this.time = time;
        this.stackTrace = stackTrace;
        this.payload = payload;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime(Date time)
    {
        this.time = time;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace)
    {
        this.stackTrace = stackTrace;
    }

    public Object getPayload()
    {
        return payload;
    }

    public void setPayload(Object payload)
    {
        this.payload = payload;
    }

    public static String convertExceptionToStackTrace(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
