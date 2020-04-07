package com.theplatform.dfh.cp.test.cleanup.endpoint;

import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.object.api.IdentifiedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Central class for tracking objects for clean up
 */
public class IdentifiedObjectCreateTracker implements IdentifiedObjectCreateListener
{
    private static final Logger logger = LoggerFactory.getLogger(IdentifiedObjectCreateTracker.class);

    private Map<Class, Set<String>> classObjectIdMap = new HashMap<>();

    public void registerForCleanup(Class clazz, String objectId)
    {
        getObjectIdSet(clazz).add(objectId);
    }

    public <T extends IdentifiedObject> void registerForCleanup(T object)
    {
        getObjectIdSet(object.getClass()).add(object.getId());
    }

    public <T extends IdentifiedObject> void registerForCleanup(DataObjectResponse<T> dataObjectResponse)
    {
        if(!dataObjectResponse.isError() && dataObjectResponse.getAll() != null)
        {
            dataObjectResponse.getAll().forEach(this::registerForCleanup);
        }
    }

    public void registerForCleanup(Class clazz, List<String> objectIds)
    {
        getObjectIdSet(clazz).addAll(objectIds);
    }

    public Map<Class, Set<String>> getClassObjectIdMap()
    {
        return classObjectIdMap;
    }

    private Set<String> getObjectIdSet(Class clazz)
    {
        if(!classObjectIdMap.containsKey(clazz))
        {
            classObjectIdMap.put(clazz, new HashSet<>());
        }
        return classObjectIdMap.get(clazz);
    }

    @Override
    public void objectCreated(IdentifiedObject dataObject)
    {
        registerForCleanup(dataObject);
    }
}

