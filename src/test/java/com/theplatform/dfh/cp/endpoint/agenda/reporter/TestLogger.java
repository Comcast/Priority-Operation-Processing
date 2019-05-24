package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import org.slf4j.helpers.SubstituteLogger;

public class TestLogger extends SubstituteLogger
{
    private StringBuilder builder = new StringBuilder();

    public TestLogger(String name)
    {
        super(name);
    }

    @Override
    public void info(String msg)
    {
        builder.append(msg);
    }

    public String getMsg()
    {
        return builder.toString();
    }
}
