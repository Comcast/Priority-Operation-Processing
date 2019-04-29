package com.theplatform.dfh.cp.handler.reaper.impl.filter;

import io.fabric8.kubernetes.api.model.Pod;

import java.util.List;

/**
 * Basic interface for looking up pods
 */
public interface PodLookupFilter
{
    /**
     * Performs the specified lookup
     * @return List of pods that passed the filter
     */
    List<Pod> performLookup();
}
