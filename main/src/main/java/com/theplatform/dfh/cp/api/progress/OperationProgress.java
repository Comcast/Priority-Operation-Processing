package com.theplatform.dfh.cp.api.progress;

import java.util.Date;

public class OperationProgress
{
    private String id;
    private OperationStatus status;
    private Long progress;
    private String title;
    private String lastErrorCode;
    private String lastErrorMessage;
    private Date lastErrorTime;
    private int attemptCount;
    private Date attemptTime;
    private Date started;
    private Date completed;
    private String payload;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public OperationStatus getStatus()
    {
        return status;
    }

    public void setStatus(OperationStatus status)
    {
        this.status = status;
    }

    public Long getProgress()
    {
        return progress;
    }

    public void setProgress(Long progress)
    {
        this.progress = progress;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getLastErrorCode()
    {
        return lastErrorCode;
    }

    public void setLastErrorCode(String lastErrorCode)
    {
        this.lastErrorCode = lastErrorCode;
    }

    public String getLastErrorMessage()
    {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage)
    {
        this.lastErrorMessage = lastErrorMessage;
    }

    public Date getLastErrorTime()
    {
        return lastErrorTime;
    }

    public void setLastErrorTime(Date lastErrorTime)
    {
        this.lastErrorTime = lastErrorTime;
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

    public Date getStarted()
    {
        return started;
    }

    public void setStarted(Date started)
    {
        this.started = started;
    }

    public Date getCompleted()
    {
        return completed;
    }

    public void setCompleted(Date completed)
    {
        this.completed = completed;
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
