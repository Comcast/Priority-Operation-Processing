package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import io.fabric8.kubernetes.client.Watcher;

public interface PodEventListener<T> extends Watcher<T>
{
}
