package com.comcast.pop.cp.endpoint.agenda.service;

import com.comcast.pop.endpoint.base.validation.DefaultRequestValidator;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.IgniteAgendaRequest;
import org.apache.commons.lang3.StringUtils;

public class IgniteAgendaServiceRequestValidator extends DefaultRequestValidator<ServiceRequest<IgniteAgendaRequest>>
{
    public static final String REQUIRED_PARAMS_MISSING = "agendaTemplateId and payload must be specified";

    @Override
    public void validatePOST(ServiceRequest<IgniteAgendaRequest> request)
    {
        IgniteAgendaRequest igniteAgendaRequest = request.getPayload();
        if(igniteAgendaRequest == null
            || StringUtils.isBlank(igniteAgendaRequest.getPayload())
            || StringUtils.isBlank(igniteAgendaRequest.getAgendaTemplateId()))
            throw new ValidationException(REQUIRED_PARAMS_MISSING);
    }
}
