package com.theplatform.dfh.endpoint.api.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.DataObjectFeedServiceResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponse;

import java.util.Collection;

public class SubmitAgendaResponse extends DataObjectFeedServiceResponse<Agenda>
{
    public SubmitAgendaResponse()
    {
    }

    public SubmitAgendaResponse(ErrorResponse errorResponse)
    {
        super(errorResponse);
    }

    public SubmitAgendaResponse(Collection<Agenda> dataObjects)
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

