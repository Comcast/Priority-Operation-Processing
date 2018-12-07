package com.theplatform.dfh.endpoint.api.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;

import java.util.Collection;

/**
 */
public class GetAgendaResponse
{

    private Collection<Agenda> agendas;

    public GetAgendaResponse(){}

    public GetAgendaResponse(Collection<Agenda> agendas)
    {
        this.agendas = agendas;
    }

    public Collection<Agenda> getAgendas()
    {
        return agendas;
    }

    public void setAgendas(Collection<Agenda> agendas)
    {
        this.agendas = agendas;
    }
}
