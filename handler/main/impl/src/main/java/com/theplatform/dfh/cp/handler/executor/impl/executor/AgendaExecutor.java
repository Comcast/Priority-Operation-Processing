package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.handler.base.perform.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public abstract class AgendaExecutor implements Executor<List<String>>
{
    public static final int MAX_MILLISECONDS_TO_READ_PROGRESS = 60000;
    protected static final String CONTAINER_NAME_PREFIX = "dfhmediainfo";
    public static final double NO_PROGRESS = 0.0;
    protected static final long POD_SCHEDULING_TIMEOUT_MS = 60000;
    public static final String MEDIAINFO_END_OF_XML = "</Mediainfo>";
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private String filePath;

    public AgendaExecutor(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFilePath()
    {
        return filePath;
    }

    protected static String generateContainerNameSuffix()
    {
        return "-" + UUID.randomUUID().toString();
    }
}