package com.theplatform.dfh.cp.handler.executor.impl.processor.runner.event;

import com.sun.tools.javac.util.StringUtils;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.AgendaProgressThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationCompleteLog implements OperationCompleteEvent
{
    private static Logger logger = LoggerFactory.getLogger(OperationCompleteLog.class);
    private ExecutorContext executorContext;

    public OperationCompleteLog(ExecutorContext executorContext)
    {
        this.executorContext = executorContext;
    }

    public void onOperationComplete(Operation operation, OperationProgress operationProgress)
    {
        String operationProgressId = OperationProgress.generateId(
            executorContext.getAgendaProgressId() == null ? "unknownProgressId" : executorContext.getAgendaProgressId(),
            operation.getName());

        long runTimeMs = -1;
        if(operationProgress.getCompletedTime() != null && operationProgress.getStartedTime() != null)
        {
            runTimeMs = operationProgress.getCompletedTime().getTime() - operationProgress.getStartedTime().getTime();
        }

        logger.info(
            "Completed operation " +
            "agendaId={} externalId={} agendaProgressId={} operationType={} operationName={} operationProgressId={} state={} conclusion={} runTime={}",
            executorContext.getAgendaId(),
            executorContext.getAgenda().getLinkId(),
            executorContext.getAgendaProgressId(),
            operation.getType(),
            operation.getName(),
            operationProgressId,
            StringUtils.toLowerCase(operationProgress.getProcessingState().name()),
            operationProgress.getProcessingStateMessage(),
            runTimeMs
        );
    }
}
