package com.theplatform.dfh.endpoint.api.agenda.service;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;

import java.util.List;

public class ExpandAgendaRequest
{
    private String agendaId;
    private List<Operation> operations;
    private ParamsMap params;

    public ExpandAgendaRequest()
    {
    }

    public String getAgendaId()
    {
        return agendaId;
    }

    public void setAgendaId(String agendaId)
    {
        this.agendaId = agendaId;
    }

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
}
