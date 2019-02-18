package com.theplatform.dfh.cp.endpoint.cleanup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EndpointObjectTrackerManager
{
    private List<EndpointObjectTracker> trackers;

    public EndpointObjectTrackerManager()
    {
        this.trackers = new ArrayList<>();
    }

    public void register(EndpointObjectTracker t)
    {
        trackers.add(t);
    }

    public void cleanUp()
    {
        trackers.forEach(EndpointObjectTracker::cleanUp);
    }
}