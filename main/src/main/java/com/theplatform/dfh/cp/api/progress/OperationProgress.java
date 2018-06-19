package com.theplatform.dfh.cp.api.progress;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Date;

public class OperationProgress
{
    private URI id;
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

    @JsonProperty
    public URI getId()
    {
        return id;
    }

    @JsonProperty
    public void setId(URI id)
    {
        this.id = id;
    }

    @JsonProperty
    public OperationStatus getStatus()
    {
        return status;
    }

    @JsonProperty
    public void setStatus(OperationStatus status)
    {
        this.status = status;
    }

    @JsonProperty
    public Long getProgress()
    {
        return progress;
    }

    @JsonProperty
    public void setProgress(Long progress)
    {
        this.progress = progress;
    }

    @JsonProperty
    public String getTitle()
    {
        return title;
    }

    @JsonProperty
    public void setTitle(String title)
    {
        this.title = title;
    }

    @JsonProperty
    public String getLastErrorCode()
    {
        return lastErrorCode;
    }

    @JsonProperty
    public void setLastErrorCode(String lastErrorCode)
    {
        this.lastErrorCode = lastErrorCode;
    }

    @JsonProperty
    public String getLastErrorMessage()
    {
        return lastErrorMessage;
    }

    @JsonProperty
    public void setLastErrorMessage(String lastErrorMessage)
    {
        this.lastErrorMessage = lastErrorMessage;
    }

    @JsonProperty
    public Date getLastErrorTime()
    {
        return lastErrorTime;
    }

    @JsonProperty
    public void setLastErrorTime(Date lastErrorTime)
    {
        this.lastErrorTime = lastErrorTime;
    }

    @JsonProperty
    public int getAttemptCount()
    {
        return attemptCount;
    }

    @JsonProperty
    public void setAttemptCount(int attemptCount)
    {
        this.attemptCount = attemptCount;
    }

    @JsonProperty
    public Date getAttemptTime()
    {
        return attemptTime;
    }

    @JsonProperty
    public void setAttemptTime(Date attemptTime)
    {
        this.attemptTime = attemptTime;
    }

    @JsonProperty
    public Date getStarted()
    {
        return started;
    }

    @JsonProperty
    public void setStarted(Date started)
    {
        this.started = started;
    }

    @JsonProperty
    public Date getCompleted()
    {
        return completed;
    }

    @JsonProperty
    public void setCompleted(Date completed)
    {
        this.completed = completed;
    }

    @JsonProperty
    public String getPayload()
    {
        return payload;
    }

    @JsonProperty
    public void setPayload(String payload)
    {
        this.payload = payload;
    }
}
