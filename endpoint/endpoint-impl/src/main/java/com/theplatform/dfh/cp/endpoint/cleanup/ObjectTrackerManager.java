package com.theplatform.dfh.cp.endpoint.cleanup;

import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObjectTrackerManager
{
    private List<ObjectTracker> trackers;
    private HashMap<Class, ObjectTracker> trackerMap;

    public ObjectTrackerManager()
    {
        this.trackers = new ArrayList<>();
        this.trackerMap = new HashMap<>();
    }

    public <T extends IdentifiedObject> ObjectTracker<T> register(ObjectTracker<T> t)
    {
        trackers.add(t);
        trackerMap.put(t.getObjectClass(), t);
        return t;
    }

    public void cleanUp()
    {
        trackers.forEach(ObjectTracker::cleanUp);
    }

    public <T extends IdentifiedObject> void track(T obj)
    {
        ObjectTracker<T> tracker = trackerMap.get(obj.getClass());
        if(tracker == null)
            throw new RuntimeServiceException(String.format("The object tracker for the object class is not found. Register your object tracker before tracking the object. " +
                "Class %s", obj.getClass()), 500);

        tracker.registerObject(obj);
    }
}