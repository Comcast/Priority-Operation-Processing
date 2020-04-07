package com.comcast.pop.callback.progress.retry;

import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaParameter;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.client.AgendaServiceClient;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class AgendaProgressProcessor
{
    private static final int MAXIMUM_AUTO_ATTEMPTS = 20;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private AgendaServiceClient agendaServiceClient;
    private ObjectClient<AgendaProgress> agendaProgressClient;

    public AgendaProgressProcessor(AgendaServiceClient agendaServiceClient, ObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaServiceClient = agendaServiceClient;
        this.agendaProgressClient = agendaProgressClient;
    }

    public void process(AgendaProgress eventAgendaProgress)
    {
        if(eventAgendaProgress == null) return;

        // retrieve the complete object
        AgendaProgress agendaProgress = getAgendaProgress(eventAgendaProgress);

        if(agendaProgress != null && shouldRetry(agendaProgress))
        {
            retryAgenda(agendaProgress);
        }
    }

    private AgendaProgress getAgendaProgress(AgendaProgress agendaProgress)
    {
        DataObjectResponse<AgendaProgress> response = agendaProgressClient.getObject(agendaProgress.getId());
        if(response.isError())
        {
            logger.error(String.format("Error retrieving AgendaProgress %1$s - %2$s", agendaProgress.getId(), response.getErrorResponse().getDescription()));
        }
        AgendaProgress fullProgress = response.getFirst();
        if(fullProgress == null)
        {
            logger.error(String.format("Missing AgendaProgress %1$s", agendaProgress.getId()));
        }
        return fullProgress;
    }

    protected boolean shouldRetry(AgendaProgress agendaProgress)
    {
        return agendaProgress.getProcessingState() == ProcessingState.COMPLETE
            && StringUtils.equalsIgnoreCase(agendaProgress.getProcessingStateMessage(), CompleteStateMessage.FAILED.name())
            && isBelowMaxAttempt(agendaProgress);
    }

    protected boolean isBelowMaxAttempt(AgendaProgress agendaProgress)
    {
        int attempts = intValueOrDefault(agendaProgress.getAttemptsCompleted(), 1);
        // don't allow the max to be some ridiculous number
        int maximumAttempts = Integer.min(MAXIMUM_AUTO_ATTEMPTS, intValueOrDefault(agendaProgress.getMaximumAttempts(), AgendaProgress.DEFAULT_MAX_ATTEMPTS));
        logInfo(agendaProgress, String.format("Current attempt status: Completed: %1$s / Maximum: %2$s", attempts, maximumAttempts));
        return attempts < maximumAttempts;
    }

    private int intValueOrDefault(Integer value, int defaultValue)
    {
        return value == null
            ? defaultValue
            : value;
    }

    protected void retryAgenda(AgendaProgress agendaProgress)
    {
        ReigniteAgendaRequest reigniteAgendaRequest = new ReigniteAgendaRequest();
        reigniteAgendaRequest.setAgendaId(agendaProgress.getAgendaId());
        // continue, not reset all
        reigniteAgendaRequest.setParams(Collections.singletonList(ReigniteAgendaParameter.CONTINUE.getParameterName()));
        try
        {
            logInfo(agendaProgress, "Reigniting");
            ReigniteAgendaResponse response = agendaServiceClient.reigniteAgenda(reigniteAgendaRequest);
            if(response.isError())
            {
                // if there is an error we likely would be unable to fix it anyway from the perspective of this component, so just log
                logError(agendaProgress, "Error when calling reignite. " + response.getErrorResponse().getDescription(), null);
            }
        }
        catch(Exception t)
        {
            logError(agendaProgress, "Failed to call reignite for Agenda", t);
            // this should cause the caller to retry as desired
            throw new RuntimeException("Failed to call reignite for Agenda", t);
        }
    }

    private void logInfo(AgendaProgress agendaProgress, String message)
    {
        logger.info(getLogMessage(agendaProgress, message));
    }

    private void logError(AgendaProgress agendaProgress, String message, Throwable t)
    {
        logger.error(getLogMessage(agendaProgress, message), t);
    }

    private String getLogMessage(AgendaProgress agendaProgress, String message)
    {
        return String.format("agendaId=%1$s customerId=%2$s %3$s",
            agendaProgress.getAgendaId(),
            agendaProgress.getCustomerId(),
            message);
    }
}
