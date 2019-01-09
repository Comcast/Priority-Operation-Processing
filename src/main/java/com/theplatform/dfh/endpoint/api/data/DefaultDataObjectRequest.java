package com.theplatform.dfh.endpoint.api.data;

import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;
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
