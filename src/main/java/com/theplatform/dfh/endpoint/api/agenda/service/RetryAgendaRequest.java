package com.theplatform.dfh.endpoint.api.agenda.service;

import java.util.List;

public class RetryAgendaRequest
{
    private String agendaId;
    private List<String> params;
    private List<String> operationsToReset;

    public RetryAgendaRequest()
    {
    }

    public RetryAgendaRequest(String agendaId, List<String> params, List<String> operationsToReset)
    {
        this.agendaId = agendaId;
        this.params = params;
        this.operationsToReset = operationsToReset;
    }

    public String getAgendaId()
    {
        return agendaId;
    }

    public void setAgendaId(String agendaId)
    {
        this.agendaId = agendaId;
    }

    public List<String> getParams()
    {
        return params;
    }

    public void setParams(List<String> params)
    {
        this.params = params;
    }

    public List<String> getOperationsToReset()
    {
        return operationsToReset;
    }

    public void setOperationsToReset(List<String> operationsToReset)
    {
        this.operationsToReset = operationsToReset;
    }
}
