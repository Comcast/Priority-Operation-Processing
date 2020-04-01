package com.comcast.fission.handler.executor.impl.processor.operation.generator;

import com.comcast.fission.endpoint.api.agenda.UpdateAgendaRequest;
import com.comcast.fission.endpoint.api.agenda.UpdateAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Simple retryable wrapper for calling the expandAgenda method
 */
public class ResourcePoolAgendaUpdater
{
    private static Logger logger = LoggerFactory.getLogger(GeneratedOperationsModifier.class);
    private int maxRetries = 0;
    private int retryDelayMs = 1000;

    /**
     * Updates the existing agenda with the supplied information
     * @param serviceClient The ResourcePoolServiceClient to communicate with
     * @param expandAgendaRequest The request to transmit
     * @return The response or null if there is an issue
     */
    public UpdateAgendaResponse update(ResourcePoolServiceClient serviceClient, UpdateAgendaRequest expandAgendaRequest)
    {
        logger.debug("Initializing retry");
        RetryPolicy<Object> policy = new RetryPolicy<>()
            .withMaxRetries(maxRetries)
            .withDelay(Duration.ofMillis(retryDelayMs));

        return Failsafe
            .with(policy)
            .onFailure(executionCompletedEvent ->
            {
                logger.warn(String.format("Attempt [%1$s / %2$s] to update agenda failed.", executionCompletedEvent.getAttemptCount(), maxRetries + 1),
                    executionCompletedEvent.getFailure());
            })
            .get((() -> serviceClient.updateAgenda(expandAgendaRequest)));
    }

    public int getMaxRetries()
    {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    public int getRetryDelayMs()
    {
        return retryDelayMs;
    }

    public void setRetryDelayMs(int retryDelayMs)
    {
        this.retryDelayMs = retryDelayMs;
    }
}
