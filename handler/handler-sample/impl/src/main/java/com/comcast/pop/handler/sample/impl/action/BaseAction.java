package com.comcast.pop.handler.sample.impl.action;

import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressReporter;
import com.comcast.pop.handler.sample.api.ActionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.IntStream;

public abstract class BaseAction
{
    private static Logger logger = LoggerFactory.getLogger(BaseAction.class);

    private static final String JOB_ID = UUID.randomUUID().toString();

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
                    sendProgress(reporter, ((double)i/(double)ITERATIONS) * 100d);
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

    protected void sendProgress(OperationProgressReporter reporter, Double progress)
    {
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put("JobId", JOB_ID);

        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setParams(paramsMap);
        operationProgress.setProcessingState(ProcessingState.EXECUTING);
        operationProgress.setPercentComplete(progress);

        reporter.addOperationProgress(operationProgress);
    }

    protected abstract void perform(OperationProgressReporter reporter, ActionParameters actionParameters);
}
