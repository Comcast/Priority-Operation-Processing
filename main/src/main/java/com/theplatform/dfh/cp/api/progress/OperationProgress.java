package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.Date;

public class OperationProgress implements IdentifiedObject
{
    private String id;
    private String agendaProgressId;
    private ProcessingState processingState;
    private String processingStateMessage;
    private String operation;
    private OperationDiagnostics[] diagnostics;
    private int attemptCount;
    private Date attemptTime;
    private Date startedTime;
    private Date completedTime;
    private String resultPayload;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

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

    public int getAttemptCount()
    {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount)
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

    public OperationDiagnostics[] getDiagnostics()
    {
        return diagnostics;
    }

    public void setDiagnostics(OperationDiagnostics[] diagnostics)
    {
        this.diagnostics = diagnostics;
    }
}
