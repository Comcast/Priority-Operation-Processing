package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import org.slf4j.helpers.SubstituteLogger;

public class CaptureLogger  extends SubstituteLogger
{
    private static final String CAPTURE_LOGGER = "capture_logger";
    private StringBuilder builder = new StringBuilder();

    public CaptureLogger()
    {
        this(CAPTURE_LOGGER);
    }

    public CaptureLogger(String name)
    {
        super(name,null, true);
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
