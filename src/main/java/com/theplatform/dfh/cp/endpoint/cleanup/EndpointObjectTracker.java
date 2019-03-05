package com.theplatform.dfh.cp.endpoint.cleanup;

import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.endpoint.client.ObjectClientException;
import com.theplatform.dfh.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointObjectTracker<T extends IdentifiedObject> extends ObjectTracker<T>
{
    private static final Logger logger = LoggerFactory.getLogger(EndpointObjectTracker.class);

    private ObjectClient<T> objectClient;

    public EndpointObjectTracker(ObjectClient<T> objectClient, Class<T> clazz)
    {
        super(clazz);
        this.objectClient = objectClient;
    }

    @Override
    public void cleanUp()
    {
        for (String id : getObjectIds())
        {
            try
            {
                objectClient.deleteObject(id);
            }
            catch (ObjectClientException e)
            {
                logger.error("Failed to delete {} with id {}", getObjectClass().getSimpleName(), id, e);
            }
        }
    }
}

