package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 */
public class AgendaProgress implements IdentifiedObject
{
    private String id;
    private String linkId;
    private String externalId;
    private String title;
    private ProcessingState processingState;
    private String processingStateMessage;
    private OperationProgress[] operationProgress;
    private Date updatedTime;
    private Date addedTime;
    private Date startedTime;
    private Date completedTime;
    private Double percentComplete;
    private String customerId;

    public AgendaProgress()
    {
    }

    public AgendaProgress(String linkId)
    {
        this.linkId = linkId;
        this.processingState = ProcessingState.WAITING;
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

    @Override
    public String getCustomerId()
    {
        return customerId;
    }

    @Override
    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

    public String getLinkId()
    {
        return linkId;
    }

    public void setLinkId(String linkId)
    {
        this.linkId = linkId;
    }

    public String getExternalId()
    {
        return externalId;
    }

    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
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

    public OperationProgress[] getOperationProgress()
    {
        return operationProgress;
    }

    public void setOperationProgress(OperationProgress[] operationProgress)
    {
        this.operationProgress = operationProgress;
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

    public void runComplete()
    {
        setCompletedTime(new Date());
        setPercentComplete(100.0);
        setProcessingState(ProcessingState.COMPLETE);
        setProcessingStateMessage("succeeded");  // todo ?
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
        AgendaProgress that = (AgendaProgress) o;
        return Objects.equals(getId(), that.getId()) &&
            Objects.equals(getLinkId(), that.getLinkId()) &&
            Objects.equals(getTitle(), that.getTitle()) &&
            getProcessingState() == that.getProcessingState() &&
            Objects.equals(getProcessingStateMessage(), that.getProcessingStateMessage()) &&
            Objects.equals(getAddedTime(), that.getAddedTime()) &&
            Objects.equals(getUpdatedTime(), that.getUpdatedTime()) &&
            Objects.equals(getStartedTime(), that.getStartedTime()) &&
            Objects.equals(getCompletedTime(), that.getCompletedTime()) &&
            Objects.equals(getPercentComplete(), that.getPercentComplete()) &&
            Arrays.equals(getOperationProgress(), that.getOperationProgress());
    }

    @Override
    public int hashCode()
    {

        int result = Objects.hash(getId(), getLinkId(), getTitle(), getProcessingState(), getProcessingStateMessage(),
            getUpdatedTime(), getAddedTime(), getStartedTime(), getCompletedTime(), getPercentComplete());
        result = 31 * result + Arrays.hashCode(getOperationProgress());
        return result;
    }
}
