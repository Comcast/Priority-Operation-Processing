package com.theplatform.dfh.cp.modules.monitor.alert;

public class AlertException extends RuntimeException
{
    public AlertException()
    {
    }

    public AlertException(String message)
    {
        super(message);
    }

    public AlertException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AlertException(Throwable cause)
    {
        super(cause);
    }

    public AlertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
