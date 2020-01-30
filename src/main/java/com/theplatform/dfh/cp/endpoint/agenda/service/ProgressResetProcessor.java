package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgressResetProcessor
{
    public void resetProgress(AgendaProgress agendaProgress, RetryAgendaRequest retryAgendaRequest, Map<RetryAgendaParameter, String> retryParameters)
    {
        Set<String> resetOperations = getResetSet(agendaProgress.getId(), retryAgendaRequest.getOperationsToReset());
        Set<String> parameters = getParameters(retryAgendaRequest.getParams());

        final boolean resetAll = parameters.contains(RetryAgendaParameter.RESET_ALL.getParameterName());

        // TODO: lots more options, reset all may be the default

        resetAgendaProgress(agendaProgress);

        if(agendaProgress.getOperationProgress() == null)
            return;

        Arrays.stream(agendaProgress.getOperationProgress())
            .filter(op -> resetOperations.contains(op.getId()) || resetAll)
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

    protected Set<String> getParameters(List<String> params)
    {
        return params == null
               ? new HashSet<>()
               : params.stream()
                   .filter(Objects::nonNull)
                   .map(StringUtils::lowerCase)
                   .collect(Collectors.toSet());
    }

    protected Set<String> getResetSet(String agendaProgressId, List<String> operationsToReset)
    {
        return operationsToReset == null
            ? new HashSet<>()
            : operationsToReset.stream()
                   .filter(Objects::nonNull)
                   .map(id -> OperationProgress.generateId(agendaProgressId, StringUtils.lowerCase(id)))
                   .collect(Collectors.toSet());
    }
}
