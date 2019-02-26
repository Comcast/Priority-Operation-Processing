package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.base.validation.DefaultRequestValidator;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class AgendaServiceValidator extends DefaultRequestValidator<ServiceRequest<GetAgendaRequest>>
{

    @Override
    public void validateGET(ServiceRequest<GetAgendaRequest> request)
    {
        super.validateGET(request);

        if (request.getPayload() == null)
            throw new ValidationException("InsightId and Count are required on getAgenda");
        validateInsight(request.getPayload().getInsightId());
        validateCount(request.getPayload().getCount());
    }

    private void validateInsight(String insightId)
    {
        if (StringUtils.isBlank(insightId))
        {
            throw new ValidationException("InsightId is required to getAgenda.");
        }
    }

    private void validateCount(Integer count)
    {
        if (count == null)
        {
            throw new ValidationException("Count is required to getAgenda.");
        }
        else if (count < 1)
        {
            throw new ValidationException("Count must be greater than 0 for getAgenda.");
        }
    }
}
