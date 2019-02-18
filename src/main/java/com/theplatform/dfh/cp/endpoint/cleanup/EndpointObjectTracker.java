package com.theplatform.dfh.cp.endpoint.cleanup;

import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.endpoint.client.ObjectClientException;
import com.theplatform.dfh.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class EndpointObjectTracker<T extends IdentifiedObject>
{
    private static final Logger logger = LoggerFactory.getLogger(EndpointObjectTracker.class);

    private ObjectClient<T> objectClient;
    private Class clazz;
    private List<String> objectIds;

    public EndpointObjectTracker(ObjectClient<T> objectClient)
    {
        this.objectClient = objectClient;
        this.clazz = objectClient.getClass();
        objectIds = new ArrayList<>();
    }

    public void registerObject(String id)
    {
        objectIds.add(id);
    }

    public void cleanUp()
    {
        for (String id : objectIds)
        {
            try
            {
                objectClient.deleteObject(id);
            }
            catch (ObjectClientException e)
            {
                logger.error("Failed to delete {} with id {}", clazz.getSimpleName(), id, e);
            }
        }
    }
}

