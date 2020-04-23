package com.comcast.pop.endpoint.api.resourcepool;

import com.comcast.pop.endpoint.api.DefaultServiceResponse;
import com.comcast.pop.endpoint.api.ErrorResponse;

public class UpdateAgendaProgressResponse extends DefaultServiceResponse
{
    public UpdateAgendaProgressResponse(){}

    public UpdateAgendaProgressResponse(ErrorResponse errorResponse)
    {
        setErrorResponse(errorResponse);
    }
}
