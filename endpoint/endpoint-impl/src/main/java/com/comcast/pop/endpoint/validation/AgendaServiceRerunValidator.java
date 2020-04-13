package com.comcast.pop.endpoint.validation;

import com.comcast.pop.endpoint.base.validation.DefaultRequestValidator;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class AgendaServiceRerunValidator extends DefaultRequestValidator<ServiceRequest<RerunAgendaRequest>>
{
    @Override
    public void validatePOST(ServiceRequest<RerunAgendaRequest> request)
    {
        super.validatePOST(request);

        if (request.getPayload() == null)
            throw new ValidationException("agendaId is required on rerun");

        validateAgendaId(request.getPayload().getAgendaId());
    }

    private void validateAgendaId(String agendaId)
    {
        if(StringUtils.isBlank(agendaId))
            throw new ValidationException("agendaId must be specified");
    }
}
