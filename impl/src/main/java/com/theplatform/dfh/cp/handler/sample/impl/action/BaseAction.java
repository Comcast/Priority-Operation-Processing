package com.theplatform.dfh.cp.handler.sample.impl.action;

import com.theplatform.dfh.cp.handler.reporter.progress.operation.OperationProgressReporter;
import com.theplatform.dfh.cp.handler.sample.api.ActionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

public abstract class BaseAction
{
    private static Logger logger = LoggerFactory.getLogger(BaseAction.class);

    public void performAction(OperationProgressReporter reporter, ActionParameters actionParameters)
    {
        Long sleepMilliseconds = actionParameters.getSleepMilliseconds();
        if(sleepMilliseconds != null && sleepMilliseconds > 0)
        {
            logger.info("Performing requested sleep: {}ms", sleepMilliseconds);
            // probably should have made the sleepMilliseconds a seconds value...
            final int ITERATIONS = Math.max(1, (int)(sleepMilliseconds/1000));
            IntStream.range(0, ITERATIONS).forEach(i ->
                {
                    reporter.addProgressOperationProgress(((double)i/(double)ITERATIONS) * 100d);
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException("Thread interrupted!", e);
                    }

                }
            );
        }
        perform(reporter, actionParameters);
    }

    protected abstract void perform(OperationProgressReporter reporter, ActionParameters actionParameters);
}
