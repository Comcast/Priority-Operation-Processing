package com.comcast.fission.endpoint.api.resourcepool;

import com.comcast.fission.endpoint.api.DefaultServiceResponse;
import com.comcast.fission.endpoint.api.ErrorResponse;

public class UpdateAgendaProgressResponse extends DefaultServiceResponse
{
    public UpdateAgendaProgressResponse(){}

    public UpdateAgendaProgressResponse(ErrorResponse errorResponse)
    {
        setErrorResponse(errorResponse);
    }
}
