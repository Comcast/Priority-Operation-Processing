package com.comcast.pop.process.helper;

import java.io.InputStream;

public class StreamGobblerFactory
{
    public StreamGobbler getStreamGobbler(InputStream inputStream)
    {
        return new StreamGobbler(inputStream);
    }
}
