package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.api.progress.WaitingStateMessage;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Processes the reset of the Agenda and OperationProgress for the retry endpoint
 */
public class ProgressResetProcessor
{
    public void resetProgress(AgendaProgress agendaProgress, ReigniteAgendaRequest reigniteAgendaRequest, Map<ReigniteAgendaParameter, String> retryParameters)
    {
        boolean resetAll = retryParameters.containsKey(ReigniteAgendaParameter.RESET_ALL);
        boolean continueOnly = retryParameters.containsKey(ReigniteAgendaParameter.CONTINUE);
        Set<String> operationsToReset = getSpecifiedOperationsToReset(retryParameters, agendaProgress);

        if (!resetAll
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
        if (agendaProgress.getOperationProgress() == null)
            return;

        Arrays.stream(agendaProgress.getOperationProgress())
            .filter(op -> operationsToReset.contains(StringUtils.lowerCase(op.getId())) || resetAll)
            .forEach(this::resetOperationProgress);
    }

    protected void resetAgendaProgress(AgendaProgress agendaProgress)
    {
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        agendaProgress.setProcessingStateMessage(WaitingStateMessage.PENDING.toString());
    }

    protected void resetOperationProgress(OperationProgress operationProgress)
    {
        operationProgress.setProcessingState(ProcessingState.WAITING);
        operationProgress.setProcessingStateMessage(WaitingStateMessage.PENDING.toString());
    }

    protected Set<String> getSpecifiedOperationsToReset(Map<ReigniteAgendaParameter, String> retryParameters, AgendaProgress agendaProgress)
    {
        if (!retryParameters.containsKey(ReigniteAgendaParameter.OPERATIONS_TO_RESET))
            return new HashSet<>();

        // validate the incoming ops to reset are valid (requires the AgendaProgress so this cannot be performed up front)
        String delimitedOps = retryParameters.get(ReigniteAgendaParameter.OPERATIONS_TO_RESET);
        String[] opsToReset = StringUtils.split(delimitedOps, ReigniteAgendaParameter.VALUE_DELIMITER);
        if (opsToReset == null || opsToReset.length == 0)
            throw new ValidationException(String.format("params is invalid - %1$s has no operations specified", ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterName()));
        Set<String> operationNames = Arrays.stream(agendaProgress.getOperationProgress()).map(OperationProgress::getOperation).collect(Collectors.toSet());
        List<String> invalidOpNames = Arrays.stream(opsToReset).filter(resetOpName -> !operationNames.contains(resetOpName)).collect(Collectors.toList());
        if (invalidOpNames.size() > 0)
            throw new ValidationException(String.format(
                "params is invalid - %1$s has the following invalid operation names: %2$s",
                ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterName(),
                String.join(",", invalidOpNames)));

        return Arrays.stream(opsToReset)
            .map(id -> StringUtils.lowerCase(OperationProgress.generateId(agendaProgress.getId(), id)))
            .collect(Collectors.toSet());
    }
}
