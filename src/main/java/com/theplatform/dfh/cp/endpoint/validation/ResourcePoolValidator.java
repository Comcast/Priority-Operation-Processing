package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class ResourcePoolValidator extends DataObjectValidator<ResourcePool, DataObjectRequest<ResourcePool>>
{
    private List<String> validationIssues;

    @Override
    public void validatePOST(DataObjectRequest<ResourcePool> request)
    {
        super.validatePOST(request);
        validationIssues = new LinkedList<>();
        ResourcePool resourcePool = request.getDataObject();
        validateCustomerId(resourcePool);
        processValidationIssues(validationIssues);
    }

    protected void validateCustomerId(ResourcePool resourcePool)
    {
        if(StringUtils.isBlank(resourcePool.getCustomerId()))
            validationIssues.add("The customerId field must be specified on the ResourcePool.");
    }
}
