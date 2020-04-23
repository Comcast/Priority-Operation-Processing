package com.comcast.pop.endpoint.api.agenda;

import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;

import java.util.List;

public class UpdateAgendaRequest
{
    private String agendaId;
    private List<Operation> operations;
    private ParamsMap params;

    public UpdateAgendaRequest()
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
