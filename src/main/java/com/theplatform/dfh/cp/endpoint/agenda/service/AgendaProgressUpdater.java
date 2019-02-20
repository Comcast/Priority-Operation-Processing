package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaProgressUpdater
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaProgressUpdater.class);

    private ObjectClient<AgendaProgress> agendaProgressClient;

    public AgendaProgressUpdater(ObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    public void updateProgress(Agenda agenda)
    {
        String progressId = agenda.getProgressId();
        if(progressId == null)
        {
            logger.warn("No progressId was found on the Agenda. Unable to update progress.");
            return;
        }

        try
        {
            DataObjectResponse<AgendaProgress> dataObjectResponse = agendaProgressClient.getObject(progressId);
            if(dataObjectResponse.isError())
            {
                logger.error("Failed to retrieve AgendaProgress id. " + dataObjectResponse.getErrorResponse().toString());
                return;
            }

            AgendaProgress agendaProgress = dataObjectResponse.getFirst();
            if(agendaProgress == null)
            {
                logger.error(String.format("No AgendaProgress was found by id: %1$s", progressId));
                return;
            }

            // TODO: bit of a truth stretch...
//            if(jobProgress.getJobStatus() == null || jobProgress.getJobStatus() == JobStatus.INITIALIZE_QUEUED)
//            {
//                jobProgress.setJobStatus(JobStatus.INITIALIZE_EXECUTING);
//            }
//            else
//            {
//                jobProgress.setJobStatus(JobStatus.RUN_EXECUTING);
//            }
            agendaProgress.setProcessingState(ProcessingState.EXECUTING);
            agendaProgressClient.updateObject(agendaProgress, agendaProgress.getId());
        }
        catch(Exception e)
        {
            logger.error("Failed to update agenda progress.", e);
        }
    }
}
