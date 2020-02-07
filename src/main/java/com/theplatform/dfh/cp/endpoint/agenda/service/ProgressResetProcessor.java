package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Processes the reset of the Agenda and OperationProgress for the retry endpoint
 */
public class ProgressResetProcessor
{
    public final static String DEFAULT_RESET_STATE_MESSAGE = "pending";

    public void resetProgress(AgendaProgress agendaProgress, ReigniteAgendaRequest reigniteAgendaRequest, Map<ReigniteAgendaParameter, String> retryParameters)
    {
        boolean resetAll = retryParameters.containsKey(ReigniteAgendaParameter.RESET_ALL);
        boolean continueOnly = retryParameters.containsKey(ReigniteAgendaParameter.CONTINUE);
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
        agendaProgress.setProcessingStateMessage(DEFAULT_RESET_STATE_MESSAGE);
    }

    protected void resetOperationProgress(OperationProgress operationProgress)
    {
        operationProgress.setProcessingState(ProcessingState.WAITING);
        operationProgress.setProcessingStateMessage(DEFAULT_RESET_STATE_MESSAGE);
    }

    protected Set<String> getSpecifiedOperationsToReset(Map<ReigniteAgendaParameter, String> retryParameters, AgendaProgress agendaProgress)
    {
        String delimitedOps = retryParameters.get(ReigniteAgendaParameter.OPERATIONS_TO_RESET);
        return StringUtils.isBlank(delimitedOps)
               ? new HashSet<>()
               : new HashSet<>(
                   Arrays.stream(StringUtils.split(delimitedOps, ReigniteAgendaParameter.VALUE_DELIMITER))
                       .map(id -> StringUtils.lowerCase(OperationProgress.generateId(agendaProgress.getId(), id)))
                       .collect(Collectors.toSet())
        );
    }
}
