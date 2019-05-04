package com.theplatform.dfh.cp.handler.reaper.impl.filter;

import io.fabric8.kubernetes.api.model.Pod;

import java.util.List;

/**
 * Basic interface for looking up pods with pagination
 */
public interface PodLookupFilter
{
    /**
     * Performs the specified lookup
     * @return List of pods that passed the filter, empty is returned if none remain
     */
    List<Pod> getNextResults();

    /**
     * Resets the lookup
     */
    void reset();
}
