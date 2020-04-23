package com.comcast.pop.endpoint.cleanup;

import com.comcast.pop.object.api.IdentifiedObject;
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
    private List<T> objects;

    public ObjectTracker(Class<T> clazz)
    {
        this.objectClass = clazz;
        objects = new ArrayList<>();
    }

    public List<T> getObjects()
    {
        return objects;
    }

    public void registerObject(T obj)
    {
        objects.add(obj);
    }

    public Class<T> getObjectClass()
    {
        return objectClass;
    }

    public abstract void cleanUp();
}
