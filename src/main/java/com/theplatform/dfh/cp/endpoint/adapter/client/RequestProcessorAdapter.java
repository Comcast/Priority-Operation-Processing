package com.theplatform.dfh.cp.endpoint.adapter.client;

import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

/**
 * Wrapper for a BaseRequestProcessor so it can be used like a client.
 * @param <T>
 */
public class RequestProcessorAdapter<T extends IdentifiedObject> implements ObjectClient<T>
{
    private final BaseRequestProcessor<T> requestProcessor;

    public RequestProcessorAdapter(BaseRequestProcessor<T> requestProcessor)
    {
        this.requestProcessor = requestProcessor;
    }

    @Override
    public DataObjectFeed<T> getObjects(String queryParams)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public DataObjectFeed<T> getObjects(List<Query> queries)
    {
        return requestProcessor.handleGET(queries);
    }

    @Override
    public T getObject(String id)
    {
        return requestProcessor.handleGET(id);
    }

    @Override
    public ObjectPersistResponse persistObject(T object)
    {
        return requestProcessor.handlePOST(object);
    }

    @Override
    public void updateObject(T object, String id)
    {
        requestProcessor.handlePUT(object);
    }

    @Override
    public void deleteObject(String id)
    {
        requestProcessor.handleDelete(id);
    }
}
