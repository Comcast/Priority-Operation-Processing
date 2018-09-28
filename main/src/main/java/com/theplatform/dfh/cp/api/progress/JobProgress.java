package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class JobProgress implements IdentifiedObject
{
    private String id;
    private String jobId;
    private JobStatus jobStatus;
    private Date updatedTime;
    private Date addedTime;
    private Date startedTime;
    private Date completedTime;
    private Double percentComplete;
    private OperationProgress[] operationProgresses;   // todo this is not plural in the doc
    private boolean halted;
    private ConclusionStatus conclusionStatus;

    public JobProgress()
    {
    }

    public JobProgress(String jobId)
    {
        this.jobId = jobId;
        this.jobStatus = JobStatus.INITIALIZE_QUEUED;
        this.addedTime = new Date();
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

    public JobStatus getJobStatus()
    {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus)
    {
        this.jobStatus = jobStatus;
    }

    public Date getUpdatedTime()
    {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime)
    {
        this.updatedTime = updatedTime;
    }

    public Date getAddedTime()
    {
        return addedTime;
    }

    public void setAddedTime(Date addedTime)
    {
        this.addedTime = addedTime;
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

    public Double getPercentComplete()
    {
        return percentComplete;
    }

    public void setPercentComplete(Double percentComplete)
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
        setCompletedTime(new Date());
        setConclusionStatus(ConclusionStatus.succeeded);
        setPercentComplete(100.0);
        setJobStatus(JobStatus.RUN_COMPLETE);
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
        return Objects.equals(getId(), that.getId()) &&
            Objects.equals(getJobId(), that.getJobId()) &&
            getJobStatus() == that.getJobStatus() &&
            Objects.equals(getAddedTime(), that.getAddedTime()) &&
            Objects.equals(getUpdatedTime(), that.getUpdatedTime()) &&
            Objects.equals(getStartedTime(), that.getStartedTime()) &&
            Objects.equals(getCompletedTime(), that.getCompletedTime()) &&
            Objects.equals(getPercentComplete(), that.getPercentComplete()) &&
            Arrays.equals(getOperationProgresses(), that.getOperationProgresses()) &&
            getConclusionStatus() == that.getConclusionStatus();
    }

    @Override
    public int hashCode()
    {

        int result = Objects.hash(getId(), getJobId(), getJobStatus(), getUpdatedTime(), getAddedTime(), getStartedTime(), getCompletedTime(), getPercentComplete(),
            getConclusionStatus());
        result = 31 * result + Arrays.hashCode(getOperationProgresses());
        return result;
    }
}
