package com.comcast.pop.endpoint.api.resourcepool;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.endpoint.api.DataObjectFeedServiceResponse;
import com.comcast.pop.endpoint.api.ErrorResponse;

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
