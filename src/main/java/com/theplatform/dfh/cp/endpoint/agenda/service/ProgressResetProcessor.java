package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgressResetProcessor
{
    public void resetProgress(AgendaProgress agendaProgress, RetryAgendaRequest retryAgendaRequest, Map<RetryAgendaParameter, String> retryParameters)
    {
        boolean resetAll = retryParameters.containsKey(RetryAgendaParameter.RESET_ALL);
        boolean continueOnly = retryParameters.containsKey(RetryAgendaParameter.CONTINUE);
        Set<String> operationsToReset = getSpecifiedOperationsToReset(retryParameters, agendaProgress);

        if(!resetAll
            && operationsToReset.size() == 0
            && !continueOnly)
        {
            // nothing was specified so default to resetting everything
            resetAll = true;
        }

        resetAgendaProgress(agendaProgress);
        resetOperationProgresses(agendaProgress, operationsToReset, resetAll);
    }

    protected void resetOperationProgresses(AgendaProgress agendaProgress, final Set<String> operationsToReset, final boolean resetAll)
    {
        if(agendaProgress.getOperationProgress() == null)
            return;

        Arrays.stream(agendaProgress.getOperationProgress())
            .filter(op -> operationsToReset.contains(StringUtils.lowerCase(op.getId())) || resetAll)
            .forEach(this::resetOperationProgress);
    }

    protected void resetAgendaProgress(AgendaProgress agendaProgress)
    {
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        agendaProgress.setProcessingStateMessage(null);
    }

    protected void resetOperationProgress(OperationProgress operationProgress)
    {
        operationProgress.setProcessingState(ProcessingState.WAITING);
        operationProgress.setProcessingStateMessage(null);
    }

    protected Set<String> getSpecifiedOperationsToReset(Map<RetryAgendaParameter, String> retryParameters, AgendaProgress agendaProgress)
    {
        String delimitedOps = retryParameters.get(RetryAgendaParameter.OPERATIONS_TO_RESET);
        return StringUtils.isBlank(delimitedOps)
               ? new HashSet<>()
               : new HashSet<>(
                   Arrays.stream(StringUtils.split(delimitedOps, RetryAgendaParameter.VALUE_DELIMITER))
                       .map(id -> StringUtils.lowerCase(OperationProgress.generateId(agendaProgress.getId(), id)))
                       .collect(Collectors.toSet())
        );
    }
}
