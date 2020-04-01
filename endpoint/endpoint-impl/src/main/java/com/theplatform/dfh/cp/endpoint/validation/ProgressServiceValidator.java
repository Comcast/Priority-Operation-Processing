package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.endpoint.base.validation.DefaultRequestValidator;
import com.comcast.fission.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.fission.endpoint.api.ServiceRequest;
import com.comcast.fission.endpoint.api.ValidationException;
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
