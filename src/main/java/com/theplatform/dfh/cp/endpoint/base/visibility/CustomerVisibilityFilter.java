package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.endpoint.api.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomerVisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest> implements VisibilityFilter<T, Req>
{
    @Override
    public boolean isVisible(Req req, T object)
    {
        return true;
        /*if(req == null || object == null) return false;

        AuthorizationResponse authorizationResponse = req.getAuthorizationResponse();
        if(authorizationResponse == null || authorizationResponse.isSuperUser()) return true;

        Set<String> authorizedCustomers = authorizationResponse.getAllowedCustomerIds();
        if(authorizedCustomers == null) return false;
        return authorizedCustomers.contains(object.getCustomerId());*/
    }

    @Override
    public List<T> filterByVisible(Req req, List<T> objects)
    {
        if(objects == null) return new ArrayList<>();
        return objects.stream().filter(o -> isVisible(req, o)).collect(Collectors.toList());
    }
}
