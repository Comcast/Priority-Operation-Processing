package com.theplatform.dfh.cp.api.progress;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Date;

public class JobProgress
{
    private URI id;
    private URI jobId;
    private JobStatus status;
    private Date added;
    private Date started;
    private Date completed;
    private Long percentComplete;
    private OperationProgress[] operationProgresses;
    private boolean halted;
    private ConclusionStatus conclusionStatus;

    public JobProgress()
    {
    }

    public JobProgress(URI jobId)
    {
        this.jobId = jobId;
        this.status = JobStatus.INITIALIZE_QUEUED;
        this.added = new Date();
    }

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
    public URI getJobId()
    {
        return jobId;
    }

    @JsonProperty
    public void setJobId(URI jobId)
    {
        this.jobId = jobId;
    }

    @JsonProperty
    public JobStatus getStatus()
    {
        return status;
    }

    @JsonProperty
    public void setStatus(JobStatus status)
    {
        this.status = status;
    }

    @JsonProperty
    public Date getAdded()
    {
        return added;
    }

    @JsonProperty
    public void setAdded(Date added)
    {
        this.added = added;
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
    public Long getPercentComplete()
    {
        return percentComplete;
    }

    @JsonProperty
    public void setPercentComplete(Long percentComplete)
    {
        this.percentComplete = percentComplete;
    }

    @JsonProperty
    public OperationProgress[] getOperationProgresses()
    {
        return operationProgresses;
    }

    @JsonProperty
    public void setOperationProgresses(OperationProgress[] operationProgresses)
    {
        this.operationProgresses = operationProgresses;
    }

    @JsonProperty
    public boolean isHalted()
    {
        return halted;
    }

    @JsonProperty
    public void setHalted(boolean halted)
    {
        this.halted = halted;
    }

    @JsonProperty
    public ConclusionStatus getConclusionStatus()
    {
        return conclusionStatus;
    }

    @JsonProperty
    public void setConclusionStatus(ConclusionStatus conclusionStatus)
    {
        this.conclusionStatus = conclusionStatus;
    }

    public void runComplete()
    {
        setCompleted(new Date());
        setConclusionStatus(ConclusionStatus.succeeded);
        setPercentComplete(100L);
        setStatus(JobStatus.RUN_COMPLETE);
    }
}
