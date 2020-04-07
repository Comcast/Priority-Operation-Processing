package com.comcast.pop.handler.puller.impl.executor.local;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local puller is just for testing/prototype. It does not perform any actual operations.
 */
public class LocalOperationLauncher implements BaseLauncher
{
    private static final Logger logger = LoggerFactory.getLogger(LocalOperationLauncher.class);
    private JsonHelper jsonHelper = new JsonHelper();

    @Override
    public void execute(Agenda agenda, AgendaProgress agendaProgress)
    {
        logger.info("Launching Executor with Payload: {} Progress: {}",
            jsonHelper.getJSONString(agenda),
            agendaProgress == null
                ? null
                : jsonHelper.getJSONString(agendaProgress)
        );
    }

}