package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.comcast.fission.endpoint.api.ServiceRequest;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponse;
import com.comcast.fission.endpoint.api.data.DataObjectRequest;
import com.comcast.fission.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

/**
 * Usage example for CreateAgenda service call and looking up the insight....
 * On top of the Agenda.customerID to Insight visibility we need to make sure our service caller's
 * authorized
 * accounts
 * match the Insight.customerID.
 *  So, the following must be true:
 *  *
 *  * 1. The calling user has an authorized account a that matches the Insight.customerId
 *  * 2. The Agenda the caller is trying to create maps to an Insight where the above is true
 *  * 3. The Agenda can only map to an Insight if one of the following is true:
 *  *     Agenda.customerId is in the Insight.allowedCustomerList
 *  *     Insight.isGlobal is true
 */
public class ServiceRequestVisibilityFilter<T extends IdentifiedObject> extends VisibilityFilter<T, DataObjectRequest<T>>
{
    private ServiceRequest serviceRequest;
    private CustomerVisibilityFilter customerVisibilityFilter = new CustomerVisibilityFilter();

    public ServiceRequestVisibilityFilter(ServiceRequest serviceRequest)
    {
        this.serviceRequest = serviceRequest;
    }

    @Override
    public boolean isVisible(DataObjectRequest<T> dataObjectRequest, T object)
    {
        //we need to verify the service caller customerID has access to the data object.
        DefaultDataObjectRequest<T> serviceCallerReq = generateServiceCallerReq(serviceRequest.getAuthorizationResponse(), object.getId());
        return customerVisibilityFilter.isVisible(serviceCallerReq, object);
    }

    private DefaultDataObjectRequest<T> generateServiceCallerReq(AuthorizationResponse authorizationResponse, String ID)
    {
        DefaultDataObjectRequest<T> req = new DefaultDataObjectRequest<>();
        req.setAuthorizationResponse(authorizationResponse);
        req.setId(ID);
        return req;
    }
}