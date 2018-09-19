package com.theplatform.dfh.cp.handler.puller.impl.executor.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local puller is just for testing/prototype. It does not perform any actual operations.
 */
public class LocalOperationLauncher implements BaseLauncher
{
    private static final Logger logger = LoggerFactory.getLogger(LocalOperationLauncher.class);

    @Override
    public void execute(String payload)
    {
        logger.info("Launching Executor with Payload: {}", payload);
    }

}