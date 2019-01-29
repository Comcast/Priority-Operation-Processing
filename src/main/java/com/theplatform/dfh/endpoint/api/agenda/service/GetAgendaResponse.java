package com.theplatform.dfh.endpoint.api.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.DefaultServiceResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponse;

import java.util.Collection;

/**
 */
public class GetAgendaResponse extends DefaultServiceResponse
{
    private Collection<Agenda> agendas;

    public GetAgendaResponse(){}

    public GetAgendaResponse(ErrorResponse errorResponse)
    {
        setErrorResponse(errorResponse);
    }

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
