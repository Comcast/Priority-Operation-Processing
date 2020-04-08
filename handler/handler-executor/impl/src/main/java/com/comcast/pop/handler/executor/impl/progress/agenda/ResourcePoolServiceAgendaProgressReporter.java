package com.comcast.pop.handler.executor.impl.progress.agenda;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressRequest;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressResponse;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http specific Reporter (special reporter for the AgendaProgress)
 */
public class ResourcePoolServiceAgendaProgressReporter implements ProgressReporter<AgendaProgress>
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private ResourcePoolServiceClient resourcePoolServiceClient;

    public ResourcePoolServiceAgendaProgressReporter(ResourcePoolServiceClient resourcePoolServiceClient)
    {
        this.resourcePoolServiceClient = resourcePoolServiceClient;
    }

    @Override
    public void reportProgress(AgendaProgress agendaProgress)
    {
        if(agendaProgress == null)
        {
            logger.warn("Attempted to report null AgendaProgress.");
            return;
        }

        ErrorResponse errorResponse = null;
        try
        {
            UpdateAgendaProgressRequest updateRequest = new UpdateAgendaProgressRequest();
            updateRequest.setAgendaProgress(agendaProgress);
            UpdateAgendaProgressResponse response = resourcePoolServiceClient.updateAgendaProgress(updateRequest);
            if(response.isError())
            {
                errorResponse = response.getErrorResponse();
            }
            else
            {
                logger.info("Successfully reported agenda progress: " + agendaProgress.getId());
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(String.format("Failed to report agenda progress: %1$s", agendaProgress.getId()), e);
        }
        if(errorResponse != null)
        {
            String message = String.format("Failed to report agenda progress: %1$s [%2$s]",
                agendaProgress.getId(), new JsonHelper().getJSONString(errorResponse));
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    @Override
    public void reportProgress(AgendaProgress agendaProgress, Object resultPayload)
    {
        throw new UnsupportedOperationException();
    }

    public void setResourcePoolServiceClient(ResourcePoolServiceClient resourcePoolServiceClient)
    {
        this.resourcePoolServiceClient = resourcePoolServiceClient;
    }
}
