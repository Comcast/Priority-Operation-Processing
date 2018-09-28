package com.theplatform.dfh.cp.handler.executor.impl.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.progress.JobProgress;
import com.theplatform.dfh.cp.api.progress.JobStatus;
import com.theplatform.dfh.cp.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressStatusUpdater
{
    private static Logger logger = LoggerFactory.getLogger(ProgressStatusUpdater.class);

    private final String progressId;
    private final HttpCPObjectClient<JobProgress> jobProgressClient;

    public ProgressStatusUpdater(String jobProgressUrl, HttpURLConnectionFactory httpURLConnectionFactory, Agenda agenda)
    {
        progressId = agenda.getParams() == null ? null : agenda.getParams().getString(GeneralParamKey.progressId);
        this.jobProgressClient = new HttpCPObjectClient<>(
            jobProgressUrl,
            httpURLConnectionFactory,
            JobProgress.class
        );
    }

    public void updateProgress()
    {
        try
        {
            JobProgress progress = jobProgressClient.getObject(progressId);
            // This is a hack
            if(progress.getJobStatus() == null || progress.getJobStatus() == JobStatus.INITIALIZE_EXECUTING)
            {
                progress.setJobStatus(JobStatus.INITIALIZE_COMPLETE);
            }
            else
            {
                progress.setJobStatus(JobStatus.RUN_COMPLETE);
            }
            jobProgressClient.updateObject(progress);
        }
        catch(Exception e)
        {
            logger.error("Failed to update status on progress object.", e);
        }
    }
}
