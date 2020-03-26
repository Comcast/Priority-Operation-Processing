package com.theplatform.dfh.endpoint.api.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.DataObjectFeedServiceResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponse;

import java.util.Collection;

public class CreateAgendaResponse extends DataObjectFeedServiceResponse<Agenda>
{
    public CreateAgendaResponse()
    {
    }

    public CreateAgendaResponse(ErrorResponse errorResponse)
    {
        super(errorResponse);
    }

    public CreateAgendaResponse(Collection<Agenda> dataObjects)
    {
        super(dataObjects);
    }

    public void setAgendas(Collection<Agenda> dataObjects)
    {
        super.setAll(dataObjects);
    }

    public Collection<Agenda> getAgendas()
    {
        return super.getAll();
    }
}