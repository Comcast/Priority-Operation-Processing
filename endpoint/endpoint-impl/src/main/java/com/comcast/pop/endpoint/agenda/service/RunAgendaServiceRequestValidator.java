package com.comcast.pop.endpoint.agenda.service;

import com.comcast.pop.endpoint.base.validation.DefaultRequestValidator;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.RunAgendaRequest;
import org.apache.commons.lang3.StringUtils;

public class RunAgendaServiceRequestValidator extends DefaultRequestValidator<ServiceRequest<RunAgendaRequest>>
{
    public static final String REQUIRED_PARAMS_MISSING = "agendaTemplateId and payload must be specified";

    @Override
    public void validatePOST(ServiceRequest<RunAgendaRequest> request)
    {
        RunAgendaRequest runAgendaRequest = request.getPayload();
        if(runAgendaRequest == null
            || StringUtils.isBlank(runAgendaRequest.getPayload())
            || StringUtils.isBlank(runAgendaRequest.getAgendaTemplateId()))
            throw new ValidationException(REQUIRED_PARAMS_MISSING);
    }
}
