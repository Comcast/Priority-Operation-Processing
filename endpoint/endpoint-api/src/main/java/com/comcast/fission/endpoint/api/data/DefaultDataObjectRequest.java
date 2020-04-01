package com.comcast.fission.endpoint.api.data;

import com.comcast.fission.endpoint.api.DefaultServiceRequest;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponse;
import com.comcast.fission.endpoint.api.auth.CustomerIdAuthorizationResponse;
import com.comcast.fission.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public class DefaultDataObjectRequest<T extends IdentifiedObject> extends DefaultServiceRequest<T> implements DataObjectRequest<T>
{
    private List<Query> queries;
    private String id;

    public DefaultDataObjectRequest()
    {
    }

    public DefaultDataObjectRequest(List<Query> queries, String id, T dataObject)
    {
        this.queries = queries;
        this.id = id;
        setDataObject(dataObject);
    }

    public static <T extends IdentifiedObject> DataObjectRequest<T> customerAuthInstance(String customerId, T payload)
    {
        DefaultDataObjectRequest<T> req = new DefaultDataObjectRequest<>();
        req.setAuthorizationResponse(new CustomerIdAuthorizationResponse(customerId));
        req.setDataObject(payload);
        if(payload != null)
            req.setId(payload.getId());
        return req;
    }
    public static <T extends IdentifiedObject> DataObjectRequest<T> serviceUserAuthInstance(T payload)
    {
        DefaultDataObjectRequest<T> req = new DefaultDataObjectRequest<>();
        req.setAuthorizationResponse(new AuthorizationResponse(null, null, null, DataVisibility.global));
        req.setDataObject(payload);
        if(payload != null)
            req.setId(payload.getId());
        return req;
    }
    public void setQueries(List<Query> queries)
    {
        this.queries = queries;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setDataObject(T dataObject)
    {
        setPayload(dataObject);
    }

    @Override
    public List<Query> getQueries()
    {
        return queries;
    }

    @Override
    public T getDataObject()
    {
        return getPayload();
    }

    @Override
    public String getId()
    {
        return id;
    }
}
