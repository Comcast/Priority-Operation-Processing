package com.theplatform.dfh.cp.api.progress;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Arrays;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JobProgress that = (JobProgress) o;

        if (isHalted() != that.isHalted())
            return false;
        if (getId() != null
            ? !getId().equals(that.getId())
            : that.getId() != null)
            return false;
        if (getJobId() != null
            ? !getJobId().equals(that.getJobId())
            : that.getJobId() != null)
            return false;
        if (getStatus() != that.getStatus())
            return false;
        if (getAdded() != null
            ? !getAdded().equals(that.getAdded())
            : that.getAdded() != null)
            return false;
        if (getStarted() != null
            ? !getStarted().equals(that.getStarted())
            : that.getStarted() != null)
            return false;
        if (getCompleted() != null
            ? !getCompleted().equals(that.getCompleted())
            : that.getCompleted() != null)
            return false;
        if (getPercentComplete() != null
            ? !getPercentComplete().equals(that.getPercentComplete())
            : that.getPercentComplete() != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getOperationProgresses(), that.getOperationProgresses()))
            return false;
        return getConclusionStatus() == that.getConclusionStatus();
    }

    @Override
    public int hashCode()
    {
        int result = getId() != null
            ? getId().hashCode()
            : 0;
        result = 31 * result + (getJobId() != null
            ? getJobId().hashCode()
            : 0);
        result = 31 * result + (getStatus() != null
            ? getStatus().hashCode()
            : 0);
        result = 31 * result + (getAdded() != null
            ? getAdded().hashCode()
            : 0);
        result = 31 * result + (getStarted() != null
            ? getStarted().hashCode()
            : 0);
        result = 31 * result + (getCompleted() != null
            ? getCompleted().hashCode()
            : 0);
        result = 31 * result + (getPercentComplete() != null
            ? getPercentComplete().hashCode()
            : 0);
        result = 31 * result + Arrays.hashCode(getOperationProgresses());
        result = 31 * result + (isHalted()
            ? 1
            : 0);
        result = 31 * result + (getConclusionStatus() != null
            ? getConclusionStatus().hashCode()
            : 0);
        return result;
    }

}
