package com.theplatform.dfh.cp.endpoint.cleanup;

import com.theplatform.dfh.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic tracker for use for cleanup when something goes wrong
 * @param <T> The type of object being tracked
 */
public abstract class ObjectTracker<T extends IdentifiedObject>
{
    private static final Logger logger = LoggerFactory.getLogger(EndpointObjectTracker.class);

    private Class<T> objectClass;
    private List<String> objectIds;

    public ObjectTracker(Class<T> clazz)
    {
        this.objectClass = clazz;
        objectIds = new ArrayList<>();
    }

    public List<String> getObjectIds()
    {
        return objectIds;
    }

    public void registerObject(String id)
    {
        objectIds.add(id);
    }

    public Class<T> getObjectClass()
    {
        return objectClass;
    }

    public abstract void cleanUp();
}
