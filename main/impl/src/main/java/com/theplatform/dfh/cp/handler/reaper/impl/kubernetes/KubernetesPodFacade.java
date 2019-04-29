package com.theplatform.dfh.cp.handler.reaper.impl.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;

import java.util.List;
import java.util.Map;

/**
 * Facade for performing basic kubernetes actions
 */
public interface KubernetesPodFacade
{
    List<Pod> lookupPods(String namespace, Map<String, String> fields);
    Boolean deletePods(List<Pod> podsToDelete);
}
