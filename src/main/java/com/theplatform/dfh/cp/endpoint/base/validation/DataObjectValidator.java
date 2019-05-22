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
    @Override
    public void validatePOST(R request)
    {
        super.validatePOST(request);
        validateObjectRequest(request, "POST");
    }

    @Override
    public void validatePUT(R request)
    {
        super.validatePUT(request);
        validateObjectRequest(request, "PUT");

        if(request.getId() == null)
            throw new ValidationException("Unable to PUT an object without specifying an id");

        T object = request.getDataObject();
        if(object.getId() != null && !object.getId().equals(request.getId()))
            throw new ValidationException(String.format("Mismatched Id in URL and on input object: URLId: %1$s and ObjectId: %2$s",
                request.getId(),
                object.getId()));
    }

    protected void validateObjectRequest(R request, String requestType)
    {
        T object = request.getDataObject();
        if(object == null)
            throw new ValidationException(String.format("Unable to %1$s a null object", requestType));
    }
}
