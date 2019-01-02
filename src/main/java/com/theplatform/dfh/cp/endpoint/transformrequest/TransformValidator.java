package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.apache.commons.lang3.StringUtils;

public class TransformValidator
{
    public void validate(TransformRequest transform)
    {
        if(StringUtils.isBlank(transform.getCustomerId()))
            throw new ValidationException("The customer id must be specified on the transform.");
    }
}
