package com.comcast.pop.api.progress;

import com.comcast.pop.api.DefaultEndpointDataObject;
import com.comcast.pop.api.params.ParamsMap;

import java.util.Date;

public class OperationProgress extends DefaultEndpointDataObject
{
    private String agendaProgressId;
    private ProcessingState processingState;
    private String processingStateMessage;
    private String operation;
    private DiagnosticEvent[] diagnosticEvents;
    private Double percentComplete;
    private Integer attemptCount;
    private Date attemptTime;
    private Date startedTime;
    private Date completedTime;
    private String resultPayload;
    private ParamsMap params;

    public String getAgendaProgressId()
    {
        return agendaProgressId;
    }

    public void setAgendaProgressId(String agendaProgressId)
    {
        this.agendaProgressId = agendaProgressId;
    }

    public ProcessingState getProcessingState()
    {
        return processingState;
    }

    public void setProcessingState(ProcessingState processingState)
    {
        this.processingState = processingState;
    }

    public String getProcessingStateMessage()
    {
        return processingStateMessage;
    }

    public void setProcessingStateMessage(String processingStateMessage)
    {
        this.processingStateMessage = processingStateMessage;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public Integer getAttemptCount()
    {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount)
    {
        this.attemptCount = attemptCount;
    }

    public Date getAttemptTime()
    {
        return attemptTime;
    }

    public void setAttemptTime(Date attemptTime)
    {
        this.attemptTime = attemptTime;
    }

    public Date getStartedTime()
    {
        return startedTime;
    }

    public void setStartedTime(Date startedTime)
    {
        this.startedTime = startedTime;
    }

    public Date getCompletedTime()
    {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime)
    {
        this.completedTime = completedTime;
    }

    public String getResultPayload()
    {
        return resultPayload;
    }

    public void setResultPayload(String resultPayload)
    {
        this.resultPayload = resultPayload;
    }

    public DiagnosticEvent[] getDiagnosticEvents()
    {
        return diagnosticEvents;
    }

    public void setDiagnosticEvents(DiagnosticEvent[] diagnosticEvents)
    {
        this.diagnosticEvents = diagnosticEvents;
    }

    public Double getPercentComplete()
    {
        return percentComplete;
    }

    public void setPercentComplete(Double percentComplete)
    {
        this.percentComplete = percentComplete;
    }

    public ParamsMap getParams()
    {
        return params;
    }

    public void setParams(ParamsMap params)
    {
        this.params = params;
    }

    public static String generateId(String agendaProgressId, String operationName)
    {
        return agendaProgressId + "-" + operationName;
    }
}
