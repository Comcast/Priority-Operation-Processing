package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.endpoint.base.validation.DefaultRequestValidator;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServiceRequestProcessor<Res extends ServiceResponse, Req extends ServiceRequest> implements RequestProcessor<Res,Req>
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceRequestProcessor.class);
    private RequestValidator<Req> requestValidator = new DefaultRequestValidator<>();
    protected abstract Res processPOST(Req req);
    protected RequestValidator<Req> getRequestValidator(){ return requestValidator; }


    public static <T extends IdentifiedObject> void addErrorForObjectNotFound(DataObjectResponse<T> serviceResponse, Class<T> retrieveClass, String id, String cid)
    {
        if(!serviceResponse.isError())
        {
            if (serviceResponse.getCount() == 0)
            {
                final String message = String.format("The %1$s specified was not found or is not visible: %2$s", retrieveClass.getSimpleName(), id);
                logger.error(message);
                ErrorResponse errorResponse = ErrorResponseFactory.badRequest(message, cid);
                serviceResponse.setErrorResponse(errorResponse);
            }
        }
    }

    @Override
    public Res handlePOST(Req request)
    {
        if(getRequestValidator() != null) getRequestValidator().validatePOST(request);

        return processPOST(request);
    }

    @Override
    public Res handleGET(Req request)
    {
        throw new BadRequestException("GET is not implemented for this endpoint");
    }
    @Override
    public Res handlePUT(Req request)
    {
        throw new BadRequestException("PUT is not implemented for this endpoint");
    }
    @Override
    public Res handleDELETE(Req request)
    {
        throw new BadRequestException("DELETE is not implemented for this endpoint");
    }

    public void setRequestValidator(RequestValidator<Req> requestValidator)
    {
        this.requestValidator = requestValidator;
    }
}
