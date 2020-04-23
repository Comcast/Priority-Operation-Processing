package com.comcast.pop.endpoint.api.agenda;

import java.util.List;

public class RerunAgendaRequest
{
    private String agendaId;
    private List<String> params;

    public RerunAgendaRequest()
    {
    }

    public RerunAgendaRequest(String agendaId, List<String> params)
    {
        this.agendaId = agendaId;
        this.params = params;
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
}
