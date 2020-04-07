package com.theplatform.dfh.cp.endpoint.validation;

import com.comcast.pop.endpoint.base.validation.DefaultRequestValidator;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class AgendaServiceReigniteValidator extends DefaultRequestValidator<ServiceRequest<ReigniteAgendaRequest>>
{
    @Override
    public void validatePOST(ServiceRequest<ReigniteAgendaRequest> request)
    {
        super.validatePOST(request);

        if (request.getPayload() == null)
            throw new ValidationException("agendaId is required on reignite");

        validateAgendaId(request.getPayload().getAgendaId());
    }

    private void validateAgendaId(String agendaId)
    {
        if(StringUtils.isBlank(agendaId))
            throw new ValidationException("agendaId must be specified");
    }
}
