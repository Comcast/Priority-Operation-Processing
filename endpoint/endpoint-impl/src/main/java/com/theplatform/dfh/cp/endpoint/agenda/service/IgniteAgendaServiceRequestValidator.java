package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.endpoint.base.validation.DefaultRequestValidator;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.IgniteAgendaRequest;
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
