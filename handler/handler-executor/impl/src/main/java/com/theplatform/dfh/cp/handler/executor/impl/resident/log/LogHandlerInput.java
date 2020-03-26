package com.theplatform.dfh.cp.handler.executor.impl.resident.log;

import java.util.List;

public class LogHandlerInput
{
    private List<String> logMessages;

    public LogHandlerInput(){}

    public LogHandlerInput(List<String> logMessages)
    {
        this.logMessages = logMessages;
    }

    public List<String> getLogMessages()
    {
        return logMessages;
    }

    public void setLogMessages(List<String> logMessages)
    {
        this.logMessages = logMessages;
    }
}
