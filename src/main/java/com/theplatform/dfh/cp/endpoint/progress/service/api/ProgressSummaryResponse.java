package com.theplatform.dfh.cp.endpoint.progress.service.api;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

import java.util.List;

public class ProgressSummaryResponse
{
    private List<AgendaProgress> progressList;
    private ProcessingState processingState;

    public ProgressSummaryResponse()
    {
    }

    public List<AgendaProgress> getProgressList()
    {
        return progressList;
    }

    public void setProgressList(List<AgendaProgress> progressList)
    {
        this.progressList = progressList;
    }

    public ProcessingState getProcessingState()
    {
        return processingState;
    }

    public void setProcessingState(ProcessingState processingState)
    {
        this.processingState = processingState;
    }
}
