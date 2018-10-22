package com.theplatform.dfh.cp.handler.puller.impl.executor.local;

import com.theplatform.dfh.cp.api.Agenda;
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
    public void execute(Agenda agenda)
    {
        logger.info("Launching Executor with Payload: {}", new JsonHelper().getJSONString(agenda));
    }

}