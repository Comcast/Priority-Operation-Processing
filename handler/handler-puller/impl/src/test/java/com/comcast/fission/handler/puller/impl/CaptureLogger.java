package com.comcast.fission.handler.puller.impl;

import org.slf4j.helpers.SubstituteLogger;

public class CaptureLogger extends SubstituteLogger
{
    private static final String CAPTURE_LOGGER = "capture_logger";
    private StringBuilder infoBuilder = new StringBuilder();
    private StringBuilder warnBuilder = new StringBuilder();

    public CaptureLogger()
    {
        this(CAPTURE_LOGGER);
    }

    public CaptureLogger(String name)
    {
        super(name, null, true);
    }

    @Override
    public void info(String msg)
    {
        infoBuilder.append(msg);
    }

    public String getInfo()
    {
        return infoBuilder.toString();
    }

    @Override
    public void warn(String msg)
    {
        warnBuilder.append(msg);
    }

    public String getWarn()
    {
        return warnBuilder.toString();
    }
}

