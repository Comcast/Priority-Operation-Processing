package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.endpoint.base.validation.DefaultRequestValidator;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaRequest;
import org.apache.commons.lang3.StringUtils;

public class ExpandAgendaServiceValidator extends DefaultRequestValidator<ServiceRequest<ExpandAgendaRequest>>
{
    @Override
    public void validatePOST(ServiceRequest<ExpandAgendaRequest> request)
    {
        super.validatePOST(request);

        if (request.getPayload() == null)
            throw new ValidationException("agendaId and operations are required on an expandAgenda request");

        ExpandAgendaRequest expandRequest = request.getPayload();

        if(StringUtils.isBlank(expandRequest.getAgendaId()))
            throw new ValidationException("agendaId is required on an expandAgenda request");

        if(expandRequest.getOperations() == null || expandRequest.getOperations().size() == 0)
            throw new ValidationException("One or more operations must be specified on an expandAgenda request");
    }
}
