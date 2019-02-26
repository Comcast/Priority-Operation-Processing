package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.endpoint.base.validation.DefaultRequestValidator;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryRequest;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class ProgressServiceValidator extends DefaultRequestValidator<ServiceRequest<ProgressSummaryRequest>>
{

    @Override
    public void validateGET(ServiceRequest<ProgressSummaryRequest> request)
    {
        super.validateGET(request);

        if (request.getPayload() == null)
            throw new ValidationException("LinkId is required to getProgressSummary.");

        if (StringUtils.isBlank(request.getPayload().getLinkId()))
        {
            throw new ValidationException("LinkId is required to getProgressSummary.");
        }
    }
}
