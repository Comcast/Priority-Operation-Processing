package com.theplatform.dfh.cp.handler.kubernetes.support.monitor;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodEventListener;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckListener;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClientException;

import java.util.List;
import java.util.Properties;

public class AliveCheckPodEventListener implements PodEventListener<Pod>, AliveCheck
{
    private PodPhase lastReceivedPodPhase;
    private AliveCheckPoller poller;

    public AliveCheckPodEventListener(Properties serviceProperties, List<AliveCheckListener> listeners)
    {
        if(listeners == null) return;
        poller = new AliveCheckPoller(serviceProperties, this, listeners);
        poller.start();
    }
    public AliveCheckPodEventListener(ConfigurationProperties alertingConfiguration, List<AliveCheckListener> listeners)
    {
        if(listeners == null) return;
        poller = new AliveCheckPoller(alertingConfiguration, this, listeners);
        poller.start();
    }

    @Override
    public void eventReceived(Action action, Pod pod)
    {
        PodStatus podStatus = pod.getStatus();
        lastReceivedPodPhase = PodPhase.fromPodStatus(podStatus);
    }

    @Override
    public boolean isAlive()
    {
        return lastReceivedPodPhase != null && (lastReceivedPodPhase == PodPhase.PENDING || lastReceivedPodPhase == PodPhase.RUNNING);
    }

    @Override
    public void onClose(KubernetesClientException cause)
    {
        poller.stop();
    }
}