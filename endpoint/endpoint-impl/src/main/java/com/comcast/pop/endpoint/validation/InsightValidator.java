package com.comcast.pop.endpoint.validation;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.endpoint.base.validation.DataObjectValidator;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
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

        validateTitle(insight);
        processValidationIssues(validationIssues);
    }

    protected void validateTitle(Insight insight)
    {
        if(StringUtils.isBlank(insight.getTitle()))
            validationIssues.add("The title field must be specified on the Insight.");
    }
}
