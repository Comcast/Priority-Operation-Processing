package com.comcast.pop.api;

import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;

import java.util.List;

public class Agenda extends DefaultEndpointDataObject
{
    private List<Operation> operations;
    private ParamsMap params;
    private String jobId; // todo remove
    private String linkId;
    private String progressId;
    private AgendaInsight agendaInsight;

    public List<Operation> getOperations()
    {
        return operations;
    }

    public void setOperations(List<Operation> operations)
    {
        this.operations = operations;
    }

    public ParamsMap getParams()
    {
        return params;
    }

    public void setParams(ParamsMap params)
    {
        this.params = params;
    }

    public String getJobId()
    {
        return jobId;
    }

    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }

    public String getLinkId()
    {
        return linkId;
    }

    public void setLinkId(String linkId)
    {
        this.linkId = linkId;
    }

    public String getProgressId()
    {
        return progressId;
    }

    public void setProgressId(String progressId)
    {
        this.progressId = progressId;
    }

    public AgendaInsight getAgendaInsight()
    {
        return agendaInsight;
    }

    public void setAgendaInsight(AgendaInsight agendaInsight)
    {
        this.agendaInsight = agendaInsight;
    }
}
