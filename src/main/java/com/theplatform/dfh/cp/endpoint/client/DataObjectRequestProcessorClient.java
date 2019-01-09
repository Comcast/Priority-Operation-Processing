package com.theplatform.dfh.cp.endpoint.client;

import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

/**
 * Wrapper for a DataObjectRequestProcessor so it can be used like a client.
 * @param <T>
 */
public class DataObjectRequestProcessorClient<T extends IdentifiedObject> implements ObjectClient<T>
{
    private final DataObjectRequestProcessor<T> requestProcessor;

    public DataObjectRequestProcessorClient(DataObjectRequestProcessor<T> requestProcessor)
    {
        this.requestProcessor = requestProcessor;
    }

    @Override
    public DataObjectResponse<T> getObjects(String queryParams)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public DataObjectResponse<T> getObjects(List<Query> queries)
    {
        DefaultDataObjectRequest<T> request = new DefaultDataObjectRequest<>();
        request.setQueries(queries);
        return requestProcessor.handleGET(request);
    }

    @Override
    public T getObject(String id)
    {
        DefaultDataObjectRequest<T> request = new DefaultDataObjectRequest<>();
        request.setId(id);
        DataObjectResponse<T> response = requestProcessor.handleGET(request);
        if(response == null) return null;
        return response.getFirst();
    }

    @Override
    public T persistObject(T object)
    {
        DefaultDataObjectRequest<T> request = new DefaultDataObjectRequest<>();
        request.setDataObject(object);
        DataObjectResponse<T> response = requestProcessor.handlePOST(request);
        return response.getFirst();
    }

    @Override
    public T updateObject(T object, String id)
    {
        DefaultDataObjectRequest<T> request = new DefaultDataObjectRequest<>();
        request.setDataObject(object);
        request.setId(id);
        DataObjectResponse<T> response = requestProcessor.handlePUT(request);
        return response.getFirst();
    }

    @Override
    public void deleteObject(String id)
    {
        DefaultDataObjectRequest<T> request = new DefaultDataObjectRequest<>();
        request.setId(id);

        requestProcessor.handleDelete(request);
    }
}
