package com.theplatform.dfh.cp.endpoint.progress.service.api;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.DefaultServiceResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponse;

import java.util.List;

public class ProgressSummaryResponse extends DefaultServiceResponse
{
    private List<AgendaProgress> progressList;
    private ProcessingState processingState;
    private Double processingPercent;

    public ProgressSummaryResponse()
    {
    }

    public ProgressSummaryResponse(ErrorResponse errorResponse)
    {
        setErrorResponse(errorResponse);
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

    public Double getProcessingPercent()
    {
        return processingPercent;
    }

    public ProgressSummaryResponse setProcessingPercent(Double processingPercent)
    {
        this.processingPercent = processingPercent;
        return this;
    }
}
