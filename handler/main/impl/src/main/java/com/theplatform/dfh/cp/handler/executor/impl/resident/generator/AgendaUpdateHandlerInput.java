package com.theplatform.dfh.cp.handler.executor.impl.resident.generator;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;

import java.util.List;

public class AgendaUpdateHandlerInput
{
    private List<Operation> operations;
    private ParamsMap params;
    private Boolean logOnly;

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

    public Boolean getLogOnly()
    {
        return logOnly;
    }

    public void setLogOnly(Boolean logOnly)
    {
        this.logOnly = logOnly;
    }
}
