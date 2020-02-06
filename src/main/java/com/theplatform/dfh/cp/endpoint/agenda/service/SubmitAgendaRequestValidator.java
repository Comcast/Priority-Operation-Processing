package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.endpoint.base.validation.DefaultRequestValidator;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaRequest;
import org.apache.commons.lang3.StringUtils;

public class SubmitAgendaRequestValidator extends DefaultRequestValidator<ServiceRequest<SubmitAgendaRequest>>
{
    public static final String REQUIRED_PARAMS_MISSING = "agendaTemplateId and payload must be specified";

    @Override
    public void validatePOST(ServiceRequest<SubmitAgendaRequest> request)
    {
        SubmitAgendaRequest submitAgendaRequest = request.getPayload();
        if(submitAgendaRequest == null
            || StringUtils.isBlank(submitAgendaRequest.getPayload())
            || StringUtils.isBlank(submitAgendaRequest.getAgendaTemplateId()))
            throw new ValidationException(REQUIRED_PARAMS_MISSING);
    }
}
