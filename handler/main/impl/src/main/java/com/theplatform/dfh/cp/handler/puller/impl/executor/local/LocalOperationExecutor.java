package com.theplatform.dfh.cp.handler.puller.impl.executor.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local puller is just for testing/prototype. It does not perform any actual operations.
 */
public class LocalOperationExecutor implements BaseLauncher
{
    private static final Logger logger = LoggerFactory.getLogger(LocalOperationExecutor.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonHelper jsonHelper;
    private static final String OUTPUT_OVERRIDE_PTR = "/resultPayload";

    public LocalOperationExecutor()
    {
        jsonHelper = new JsonHelper();
    }

    @Override
    public void execute(String payload)
    {
        logger.info("Launching Executor with Payload: {}", payload);
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }
}