package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.comcast.fission.endpoint.api.ServiceRequest;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponse;
import com.comcast.fission.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This is the default visibility filter.
 * Visibility is allowed if one the following are true:
 * - Calling user isGlobal (super or service user with * access)
 * - Object.customerId is within the Authorized customer list (allowed accounts)
 * @param <T> Data Object
 * @param <Req> Incoming endpoint request
 */
public class CustomerVisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest> extends VisibilityFilter<T, Req>
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

        AuthorizationResponse authorizationResponse = req.getAuthorizationResponse();
        if(authorizationResponse == null)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No authorized response available.");
            return false;
        }

        //If the visibility is global, we are ok to see all data.
        if(authorizationResponse.getVisibility() == DataVisibility.global)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = {}", DataVisibility.global);
            return true;
        }

        Set<String> authorizedCustomers = authorizationResponse.getAllowedCustomerIds();
        if(authorizedCustomers == null || authorizedCustomers.size() == 0)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No authorized accounts.");
            return false;
        }

        boolean authorizedAccount = authorizedCustomers.stream().anyMatch(ac -> ac != null && ac.equals(object.getCustomerId()));
         if(logger.isDebugEnabled())
            logger.debug("visibility = {}", authorizedAccount ? DataVisibility.authorized_account : "false. No authorized accounts available.");

        return authorizedAccount;
    }

}
