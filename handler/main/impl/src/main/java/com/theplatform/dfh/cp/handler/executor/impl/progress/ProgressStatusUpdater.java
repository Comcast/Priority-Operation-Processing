package com.theplatform.dfh.cp.handler.executor.impl.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressStatusUpdater
{
    private static Logger logger = LoggerFactory.getLogger(ProgressStatusUpdater.class);

    private final String progressId;
    private final HttpCPObjectClient<AgendaProgress> agendaProgressClient;

    public ProgressStatusUpdater(String agendaProgressUrl, HttpURLConnectionFactory httpURLConnectionFactory, Agenda agenda)
    {
        progressId = agenda.getParams() == null ? null : agenda.getParams().getString(GeneralParamKey.progressId);
        this.agendaProgressClient = new HttpCPObjectClient<>(
            agendaProgressUrl,
            httpURLConnectionFactory,
            AgendaProgress.class
        );
    }

    public void updateProgress()
    {
        if(progressId == null)
        {
            logger.warn("ProgressId is null. Unable to adjust status.");
            return;
        }

        try
        {
            AgendaProgress progress = agendaProgressClient.getObject(progressId);
            progress.setProcessingState(ProcessingState.COMPLETE);
            agendaProgressClient.updateObject(progress);
        }
        catch(Exception e)
        {
            logger.error("Failed to update status on progress object.", e);
        }
    }
}
