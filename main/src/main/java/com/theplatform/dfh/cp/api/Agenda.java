package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;

import java.util.List;

public class Agenda implements IdentifiedObject
{
    private List<Operation> operations;
    private ParamsMap params;
    private String id;
    private String jobId; // todo remove
    private String linkId;

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

    public String getLinkId()
    {
        return linkId;
    }

    public void setLinkId(String linkId)
    {
        this.linkId = linkId;
    }
}
