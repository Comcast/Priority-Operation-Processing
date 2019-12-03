package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class InsightValidator extends DataObjectValidator<Insight, DataObjectRequest<Insight>>
{
    private List<String> validationIssues;

    @Override
    public void validatePOST(DataObjectRequest<Insight> request)
    {
        super.validatePOST(request);
        validationIssues = new LinkedList<>();
        Insight insight = request.getDataObject();

        validateCustomerId(insight);
        validateTitle(insight);
        processValidationIssues(validationIssues);
    }

    protected void validateCustomerId(Insight insight)
    {
        if(StringUtils.isBlank(insight.getCustomerId()))
            validationIssues.add("The customerId field must be specified on the Insight.");
    }

    protected void validateTitle(Insight insight)
    {
        if(StringUtils.isBlank(insight.getTitle()))
            validationIssues.add("The title field must be specified on the Insight.");
    }
}
