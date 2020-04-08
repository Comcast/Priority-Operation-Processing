package com.comcast.pop.test.cleanup.endpoint;

import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.client.HttpObjectClient;
import com.comcast.pop.http.api.HttpURLConnectionFactory;
import com.comcast.pop.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for HttpObjectClient so we don't have to track every persist after making this call. Routes all results from POST/persists
 * to a IdentifiedObjectCreateTracker for clean up
 * @param <T> The type of object the HttpObjectClient reads/writes
 */
public class TrackedHttpObjectClient<T extends IdentifiedObject> extends HttpObjectClient<T>
{
    private List<IdentifiedObjectCreateListener> onCreateListeners = new ArrayList<>();

    public TrackedHttpObjectClient(String endpointURL, HttpURLConnectionFactory httpUrlConnectionFactory, Class<T> clazz)
    {
        super(endpointURL, httpUrlConnectionFactory, clazz);
    }

    public TrackedHttpObjectClient(String endpointURL, HttpURLConnectionFactory httpUrlConnectionFactory, Class<T> clazz,
        IdentifiedObjectCreateListener onCreateListener)
    {
        super(endpointURL, httpUrlConnectionFactory, clazz);
        addOnCreateListener(onCreateListener);
    }

    public void setOnCreateListener(IdentifiedObjectCreateListener listener)
    {
        onCreateListeners.add(listener);
    }

    public void addOnCreateListener(IdentifiedObjectCreateListener listener)
    {
        onCreateListeners.add(listener);
    }

    public void sendObjectCreate(T object)
    {
        onCreateListeners.forEach(listener -> listener.objectCreated(object));
    }

    @Override
    public DataObjectResponse<T> persistObject(T object)
    {
        DataObjectResponse<T> response = super.persistObject(object);
        if(response != null && !response.isError() && response.getAll() != null)
        {
            response.getAll().forEach(this::sendObjectCreate);
        }
        return response;
    }
}
