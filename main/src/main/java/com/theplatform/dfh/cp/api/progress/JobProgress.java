package com.theplatform.dfh.cp.api.progress;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class JobProgress
{
    private String id;
    private String jobId;
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

    public JobProgress(String jobId)
    {
        this.jobId = jobId;
        this.status = JobStatus.INITIALIZE_QUEUED;
        this.added = new Date();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getJobId()
    {
        return jobId;
    }

    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }

    public JobStatus getStatus()
    {
        return status;
    }

    public void setStatus(JobStatus status)
    {
        this.status = status;
    }

    public Date getAdded()
    {
        return added;
    }

    public void setAdded(Date added)
    {
        this.added = added;
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

    public Long getPercentComplete()
    {
        return percentComplete;
    }

    public void setPercentComplete(Long percentComplete)
    {
        this.percentComplete = percentComplete;
    }

    public OperationProgress[] getOperationProgresses()
    {
        return operationProgresses;
    }

    public void setOperationProgresses(OperationProgress[] operationProgresses)
    {
        this.operationProgresses = operationProgresses;
    }

    public boolean isHalted()
    {
        return halted;
    }

    public void setHalted(boolean halted)
    {
        this.halted = halted;
    }

    public ConclusionStatus getConclusionStatus()
    {
        return conclusionStatus;
    }

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
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        JobProgress that = (JobProgress) o;
        return isHalted() == that.isHalted() &&
            Objects.equals(getId(), that.getId()) &&
            Objects.equals(getJobId(), that.getJobId()) &&
            getStatus() == that.getStatus() &&
            Objects.equals(getAdded(), that.getAdded()) &&
            Objects.equals(getStarted(), that.getStarted()) &&
            Objects.equals(getCompleted(), that.getCompleted()) &&
            Objects.equals(getPercentComplete(), that.getPercentComplete()) &&
            Arrays.equals(getOperationProgresses(), that.getOperationProgresses()) &&
            getConclusionStatus() == that.getConclusionStatus();
    }

    @Override
    public int hashCode()
    {

        int result = Objects.hash(getId(), getJobId(), getStatus(), getAdded(), getStarted(), getCompleted(), getPercentComplete(), isHalted(), getConclusionStatus());
        result = 31 * result + Arrays.hashCode(getOperationProgresses());
        return result;
    }
}
