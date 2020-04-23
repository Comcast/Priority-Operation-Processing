package com.comcast.pop.endpoint.cleanup;

import com.comcast.pop.endpoint.base.DataObjectRequestProcessor;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.endpoint.client.ObjectClientException;
import com.comcast.pop.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointObjectTracker<T extends IdentifiedObject> extends ObjectTracker<T>
{
    private static final Logger logger = LoggerFactory.getLogger(EndpointObjectTracker.class);

    private DataObjectRequestProcessor<T> objectClient;
    private String customerId;

    public EndpointObjectTracker(DataObjectRequestProcessor<T> objectClient, Class<T> clazz, String customerId)
    {
        super(clazz);
        this.objectClient = objectClient;
        this.customerId = customerId;
    }

    @Override
    public void cleanUp()
    {
        for (T obj : getObjects())
        {
            if(obj == null || obj.getId() == null) continue;
            try
            {
                DataObjectRequest<T> request = DefaultDataObjectRequest.customerAuthInstance(customerId, obj);
                DataObjectResponse<T> response = objectClient.handleDELETE(request);
                if(response != null && response.isError() && response.getErrorResponse() != null)
                    logger.error("Failed to delete {} with id {}", getObjectClass().getSimpleName(), obj.getId(), response.getErrorResponse().getDescription());
            }
            catch (ObjectClientException e)
            {
                logger.error("Failed to delete {} with id {}", getObjectClass().getSimpleName(), obj.getId(), e);
            }
        }
    }

}

