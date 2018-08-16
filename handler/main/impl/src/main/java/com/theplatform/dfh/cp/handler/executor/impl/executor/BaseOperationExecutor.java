package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * TODO: rename to anything else...
 */
public abstract class BaseOperationExecutor
{
    public static final int MAX_MILLISECONDS_TO_READ_PROGRESS = 60000;
    protected static final String CONTAINER_NAME_PREFIX = "dfhmediainfo";
    public static final double NO_PROGRESS = 0.0;
    protected static final long POD_SCHEDULING_TIMEOUT_MS = 60000;
    public static final String MEDIAINFO_END_OF_XML = "</Mediainfo>";
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Operation operation;

    public BaseOperationExecutor(Operation operation)
    {
        this.operation = operation;
    }

    public abstract String execute(String payload);

    protected static String generateContainerNameSuffix()
    {
        return "-" + UUID.randomUUID().toString();
    }
}