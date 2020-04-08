package com.comcast.pop.cp.endpoint.validation;

import com.comcast.pop.endpoint.base.validation.DefaultRequestValidator;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class ProgressServiceValidator extends DefaultRequestValidator<ServiceRequest<ProgressSummaryRequest>>
{

    @Override
    public void validatePOST(ServiceRequest<ProgressSummaryRequest> request)
    {
        super.validatePOST(request);

        if (request.getPayload() == null)
            throw new ValidationException("LinkId is required to call getProgressSummary.");

        if (StringUtils.isBlank(request.getPayload().getLinkId()))
        {
            throw new ValidationException("LinkId is required to call getProgressSummary.");
        }
    }
}
