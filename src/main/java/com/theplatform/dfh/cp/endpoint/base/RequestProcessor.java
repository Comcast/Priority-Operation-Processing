package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ServiceResponse;

/**
 * RequestProcessor base that forces validation (if implemented) before the requests are handled.
 * @param <Res> The response type
 * @param <Req> The request type
 */
public abstract class RequestProcessor<Res extends ServiceResponse, Req extends ServiceRequest>
{
    /**
     * Processes the GET request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res processGET(Req request)
    {
        if(getRequestValidator() != null) getRequestValidator().validateGET(request);
        return handleGET(request);
    }
    /**
     * Processes the POST request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res processPOST(Req request)
    {
        if(getRequestValidator() != null) getRequestValidator().validatePOST(request);
        return handlePOST(request);
    }
    /**
     * Processes the PUT request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res processPUT(Req request)
    {
        if(getRequestValidator() != null) getRequestValidator().validatePUT(request);
        return handlePUT(request);
    }
    /**
     * Processes the DELETE request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res processDELETE(Req request)
    {
        if(getRequestValidator() != null) getRequestValidator().validateDELETE(request);
        return handleDELETE(request);
    }

    protected Res handleGET(Req request)
    {
        throw new BadRequestException("GET is not implemented for this endpoint");
    }

    protected Res handlePOST(Req request)
    {
        throw new BadRequestException("POST is not implemented for this endpoint");
    }

    protected Res handlePUT(Req request)
    {
        throw new BadRequestException("PUT is not implemented for this endpoint");
    }

    protected Res handleDELETE(Req request)
    {
        throw new BadRequestException("DELETE is not implemented for this endpoint");
    }

    public RequestValidator<Req> getRequestValidator()
    {
        return null;
    }
}
