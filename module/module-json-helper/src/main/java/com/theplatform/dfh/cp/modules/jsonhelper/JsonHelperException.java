package com.theplatform.dfh.cp.modules.jsonhelper;

public class JsonHelperException extends RuntimeException
{
    public JsonHelperException(String message)
    {
        super(message);
    }

    public JsonHelperException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JsonHelperException(Throwable cause)
    {
        super(cause);
    }
}
