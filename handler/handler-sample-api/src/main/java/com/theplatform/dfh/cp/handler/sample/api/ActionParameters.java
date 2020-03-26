package com.theplatform.dfh.cp.handler.sample.api;

public class ActionParameters
{
    private Long sleepMilliseconds;
    private String logMessage;
    private String[] externalArgs;
    private String exceptionMessage;

    public Long getSleepMilliseconds()
    {
        return sleepMilliseconds;
    }

    public void setSleepMilliseconds(Long sleepMilliseconds)
    {
        this.sleepMilliseconds = sleepMilliseconds;
    }

    public String getLogMessage()
    {
        return logMessage;
    }

    public void setLogMessage(String logMessage)
    {
        this.logMessage = logMessage;
    }

    public String[] getExternalArgs()
    {
        return externalArgs;
    }

    public void setExternalArgs(String[] parameters)
    {
        this.externalArgs = parameters;
    }

    public String getExceptionMessage()
    {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage)
    {
        this.exceptionMessage = exceptionMessage;
    }
}
