package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomerVisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest> implements VisibilityFilter<T, Req>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean isVisible(Req req, T object)
    {
        if(req == null || object == null) return false;

        AuthorizationResponse authorizationResponse = req.getAuthorizationResponse();
        if(authorizationResponse == null)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No authorized response available.");
            return true; //false
        }

        //If the visibility is global, we are ok to see all data.
        if(authorizationResponse.getVisibility() == DataVisibility.global)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = " +DataVisibility.global);
            return true;
        }

        Set<String> authorizedCustomers = authorizationResponse.getAllowedCustomerIds();
        if(authorizedCustomers == null)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No authorized accounts available.");
            return true; //false
        }

        final boolean inAllowedAccounts = authorizedCustomers.contains(object.getCustomerId());
        if(inAllowedAccounts && logger.isDebugEnabled()) logger.debug("visibility = " +DataVisibility.authorized_account);
        return true; //inAllowedAccounts;
    }

    @Override
    public List<T> filterByVisible(Req req, List<T> objects)
    {
        if(objects == null) return new ArrayList<>();
        return objects.stream().filter(o -> isVisible(req, o)).collect(Collectors.toList());
    }
}
