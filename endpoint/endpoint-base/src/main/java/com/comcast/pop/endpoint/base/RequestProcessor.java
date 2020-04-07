package com.comcast.pop.endpoint.base;

import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ServiceResponse;

/**
 * RequestProcessor base that forces validation (if implemented) before the requests are handled.
 * @param <Res> The response type
 * @param <Req> The request type
 */
public interface RequestProcessor<Res extends ServiceResponse, Req extends ServiceRequest>
{

    /**
     * Processes the GET request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res handleGET(Req request);
    /**
     * Processes the POST request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res handlePOST(Req request);
    /**
     * Processes the PUT request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res handlePUT(Req request);
    /**
     * Processes the DELETE request, validating then handling.
     * @param request The request to process
     * @return The response object
     */
    public Res handleDELETE(Req request);

}
