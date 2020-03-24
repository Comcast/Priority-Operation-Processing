package com.theplatform.dfh.endpoint.api.resourcepool.service;

import com.theplatform.dfh.endpoint.api.DefaultServiceResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponse;

public class UpdateAgendaProgressResponse extends DefaultServiceResponse
{
    public UpdateAgendaProgressResponse(){}

    public UpdateAgendaProgressResponse(ErrorResponse errorResponse)
    {
        setErrorResponse(errorResponse);
    }
}
