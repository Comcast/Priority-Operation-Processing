package com.theplatform.dfh.cp.agenda.reclaim.exception;

public class ReclaimerException extends RuntimeException
{
    public ReclaimerException(String message)
    {
        super(message);
    }

    public ReclaimerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ReclaimerException(Throwable cause)
    {
        super(cause);
    }

    protected ReclaimerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
