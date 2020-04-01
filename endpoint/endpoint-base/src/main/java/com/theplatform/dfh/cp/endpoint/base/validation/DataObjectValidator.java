package com.theplatform.dfh.cp.endpoint.base.validation;

import com.comcast.fission.endpoint.api.ValidationException;
import com.comcast.fission.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

/**
 * Basic validator for object requests.
 * @param <R> The request type
 */
public class DataObjectValidator<T extends IdentifiedObject, R extends DataObjectRequest<T>> extends DefaultRequestValidator<R>
{
    private static final String UNABLE_TO_PROCESS_NULL_EXCEPTION = "Unable to %1$s a null object";
    private static final String UNABLE_TO_PUT_WITHOUT_ID_EXCEPTION = "Unable to PUT an object without specifying an id";
    private static final String MISMATCHED_PUT_ID_EXCEPTION = "Mismatched Id in URL and on input object: URLId: %1$s and ObjectId: %2$s";
    private static final String CUSTOMER_ID_NOT_SPECIFIED = "The customerId field must be specified.";

    @Override
    public void validatePOST(R request)
    {
        super.validatePOST(request);
        validateObjectRequest(request, "POST");

        T object = request.getDataObject();

        if(object.getCustomerId() == null || object.getCustomerId().trim().length() == 0)
            throw new ValidationException(CUSTOMER_ID_NOT_SPECIFIED);
    }

    @Override
    public void validatePUT(R request)
    {
        super.validatePUT(request);
        validateObjectRequest(request, "PUT");

        if(request.getId() == null)
            throw new ValidationException(UNABLE_TO_PUT_WITHOUT_ID_EXCEPTION);

        T object = request.getDataObject();
        if(object.getId() != null && !object.getId().equals(request.getId()))
            throw new ValidationException(String.format(MISMATCHED_PUT_ID_EXCEPTION, request.getId(), object.getId()));
    }

    protected void validateObjectRequest(R request, String requestType)
    {
        T object = request.getDataObject();
        if(object == null)
            throw new ValidationException(String.format(UNABLE_TO_PROCESS_NULL_EXCEPTION, requestType));
    }
}
