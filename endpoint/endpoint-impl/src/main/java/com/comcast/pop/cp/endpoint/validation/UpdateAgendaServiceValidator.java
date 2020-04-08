package com.comcast.pop.cp.endpoint.validation;

import com.comcast.pop.endpoint.base.validation.DefaultRequestValidator;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaRequest;
import org.apache.commons.lang3.StringUtils;

public class UpdateAgendaServiceValidator extends DefaultRequestValidator<ServiceRequest<UpdateAgendaRequest>>
{
    @Override
    public void validatePOST(ServiceRequest<UpdateAgendaRequest> request)
    {
        super.validatePOST(request);

        if (request.getPayload() == null)
            throw new ValidationException("agendaId and operations are required on an updateAgenda request");

        UpdateAgendaRequest expandRequest = request.getPayload();

        if(StringUtils.isBlank(expandRequest.getAgendaId()))
            throw new ValidationException("agendaId is required on an updateAgenda request");

        if(expandRequest.getOperations() == null || expandRequest.getOperations().size() == 0)
            throw new ValidationException("One or more operations must be specified on an updateAgenda request");
    }
}
