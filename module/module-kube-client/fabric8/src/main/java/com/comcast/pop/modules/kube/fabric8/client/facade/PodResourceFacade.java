package com.comcast.pop.modules.kube.fabric8.client.facade;

import com.comcast.pop.modules.kube.fabric8.client.watcher.PodWatcherImpl;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.LogWatch;

public interface PodResourceFacade
{
    Pod get();
    String getLog();
    Watch watch(PodWatcherImpl podWatcherImpl);
    LogWatch watchLog();
}
