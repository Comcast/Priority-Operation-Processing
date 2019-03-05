package com.theplatform.dfh.cp.endpoint.cleanup;

import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class ObjectTrackerManager
{
    private List<ObjectTracker> trackers;

    public ObjectTrackerManager()
    {
        this.trackers = new ArrayList<>();
    }

    public <T extends IdentifiedObject> ObjectTracker<T> register(ObjectTracker<T> t)
    {
        trackers.add(t);
        return t;
    }

    public void cleanUp()
    {
        trackers.forEach(ObjectTracker::cleanUp);
    }
}