package com.theplatform.dfh.cp.endpoint.base.validation;

import com.comcast.fission.endpoint.api.ServiceRequest;
import com.comcast.fission.endpoint.api.ValidationException;

import java.util.List;

/**
 * Basic request validation
 * @param <R> The type of request
 */
public class DefaultRequestValidator<R extends ServiceRequest> implements RequestValidator<R>
{
    public static final String VALIDATION_TRUNCATION_MESSAGE = "[Truncating additional issues]";
    private static final int MAX_VALIDATION_ISSUES_TO_REPORT = 10;

    @Override
    public void validateGET(R request)
    {
        validateRequest(request);
    }

    @Override
    public void validatePOST(R request)
    {
        validateRequest(request);
    }

    @Override
    public void validatePUT(R request)
    {
        validateRequest(request);
    }

    @Override
    public void validateDELETE(R request)
    {
        validateRequest(request);
    }

    protected void validateRequest(R request)
    {
        if(request == null)
            throw new ValidationException("The request cannot be null.");
    }

    protected int getMaxValidationIssuesToReport()
    {
        return MAX_VALIDATION_ISSUES_TO_REPORT;
    }

    /**
     * Throws a ValidationException if any validationIssues are present
     * @param validationIssues The list of validationIssues to report in the exception
     */
    public void processValidationIssues(List<String> validationIssues)
    {
        if(validationIssues == null || validationIssues.size() == 0)
            return;

        int lastIssueIndex = Math.min(validationIssues.size(), getMaxValidationIssuesToReport());
        throw new ValidationException(String.format("Issues detected: %1$s%2$s",
            String.join(",", validationIssues.subList(0, lastIssueIndex)),
            lastIssueIndex < validationIssues.size() ? VALIDATION_TRUNCATION_MESSAGE : "")
        );
    }
}
