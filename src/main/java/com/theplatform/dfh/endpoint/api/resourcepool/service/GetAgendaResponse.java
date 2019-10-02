package com.theplatform.dfh.endpoint.api.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.DataObjectFeedServiceResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponse;

import java.util.Collection;

public class GetAgendaResponse extends DataObjectFeedServiceResponse<Agenda>
{
    public GetAgendaResponse()
    {
    }

    public GetAgendaResponse(ErrorResponse errorResponse)
    {
        super(errorResponse);
    }

    public GetAgendaResponse(Collection<Agenda> dataObjects)
    {
        super(dataObjects);
    }
}
