package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.Date;

public class OperationProgress implements IdentifiedObject
{
    private String id;
    private String jobProgressId;
    private OperationStatus status;
    private Double percentComplete;
    private String operation;
    private String diagnosticId;
    private int attemptCount;
    private Date attemptTime;
    private Date startedTime;
    private Date completedTime;
    private String payload;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getJobProgressId()
    {
        return jobProgressId;
    }

    public void setJobProgressId(String jobProgressId)
    {
        this.jobProgressId = jobProgressId;
    }

    public OperationStatus getStatus()
    {
        return status;
    }

    public void setStatus(OperationStatus status)
    {
        this.status = status;
    }

    public Double getPercentComplete()
    {
        return percentComplete;
    }

    public void setPercentComplete(Double percentComplete)
    {
        this.percentComplete = percentComplete;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public String getDiagnosticId()
    {
        return diagnosticId;
    }

    public void setDiagnosticId(String diagnosticId)
    {
        this.diagnosticId = diagnosticId;
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

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }
}
