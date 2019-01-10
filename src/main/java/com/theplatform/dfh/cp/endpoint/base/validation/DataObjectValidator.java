package com.theplatform.dfh.cp.endpoint.base.validation;

import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

/**
 * Basic validator for object requests.
 * @param <R> The request type
 */
public class DataObjectValidator<T extends IdentifiedObject, R extends DataObjectRequest<T>> extends DefaultRequestValidator<R>
{
    // TODO: some more...

    @Override
    public void validatePOST(R request)
    {
        super.validatePOST(request);
        T object = request.getDataObject();
        if(object == null)
            throw new ValidationException("Unable to POST a null object");
    }

    @Override
    public void validatePUT(R request)
    {
        super.validatePUT(request);
        T object = request.getDataObject();
        if(object == null)
            throw new ValidationException("Unable to PUT a null object");
    }
}
