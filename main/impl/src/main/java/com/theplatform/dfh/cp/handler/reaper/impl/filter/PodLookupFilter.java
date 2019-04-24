package com.theplatform.dfh.cp.handler.reaper.impl.filter;

import io.fabric8.kubernetes.api.model.Pod;

import java.util.List;

public interface PodLookupFilter
{
    List<Pod> performLookup();
}
