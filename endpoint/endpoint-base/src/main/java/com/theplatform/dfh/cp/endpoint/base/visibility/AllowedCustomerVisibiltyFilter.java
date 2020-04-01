package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.cp.api.AllowedCustomerEndpointDataObject;
import com.comcast.fission.endpoint.api.ServiceRequest;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponse;
import com.comcast.fission.endpoint.api.auth.DataVisibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class AllowedCustomerVisibiltyFilter<T extends AllowedCustomerEndpointDataObject, Req extends ServiceRequest> extends VisibilityFilter<T, Req>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean isVisible(Req req, T object)
    {
        if(req == null || object == null)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No request or data object");
            return false;
        }

        if(object.getAllowedCustomerIds() == null || object.getAllowedCustomerIds().size() == 0)
            return false;

        AuthorizationResponse authorizationResponse = req.getAuthorizationResponse();
        if(authorizationResponse == null)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No authorized response available.");
            return false;
        }
        Set<String> authorizedCustomers = authorizationResponse.getAllowedCustomerIds();
        if(authorizedCustomers == null)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No authorized accounts.");
            return false;
        }
        Set<String> allowedCustomersOnObject = object.getAllowedCustomerIds();
        boolean authorizedAccount = authorizedCustomers.stream().anyMatch(ac -> ac != null && allowedCustomersOnObject.contains(ac.toLowerCase()));
        if(logger.isDebugEnabled())
            logger.debug("visibility = {}", authorizedAccount ? DataVisibility.authorized_account : "false. No authorized accounts available.");


        return authorizedAccount;
    }
}
