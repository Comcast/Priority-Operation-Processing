package com.comcast.pop.handler.kubernetes.support.monitor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodEventListener;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodPhase;
import com.comcast.pop.modules.monitor.metric.MetricLabel;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClientException;


public class MetricPodEventListener implements PodEventListener<Pod>
{
    private Meter podDeletedMetric;
    private Meter podFailedMetric;
    private Timer podDurationMetric;
    private Timer.Context lastDurationContext;
    private PodPhase lastReceivedPodPhase;
    private MetricReporter metricReporter;

    public MetricPodEventListener(MetricReporter metricReporter)
    {
        if(metricReporter == null || metricReporter.getMetricRegistry() == null) throw new RuntimeException("Unable to start the pod watcher. No metric reporter found.");
        MetricRegistry registry = metricReporter.getMetricRegistry();
        this.metricReporter = metricReporter;
        this.podDeletedMetric = registry.meter("pod." + MetricLabel.deleted.name());
        this.podFailedMetric = registry.meter("pod." + MetricLabel.failed.name());
        this.podDurationMetric = registry.timer("pod." + MetricLabel.duration.name());
    }

    @Override
    public void eventReceived(final Action action, Pod pod)
    {
        PodStatus podStatus = pod.getStatus();
        lastReceivedPodPhase = PodPhase.fromPodStatus(podStatus);
        switch(action)
        {
            case DELETED:
            {
                podDeletedMetric.mark();
                stopDurationTimer();
                //report now since our pod will shutdown.
                metricReporter.report();
            }
            case ADDED:
            {
                startDurationTimer();
            }
            default:
            {
                if(lastDurationContext == null && lastReceivedPodPhase.equals(PodPhase.PENDING))
                {
                    startDurationTimer();
                }
                else if (lastReceivedPodPhase.equals(PodPhase.FAILED))
                {
                    podFailedMetric.mark();
                    stopDurationTimer();
                    //report now since our pod will shutdown.
                    metricReporter.report();
                }
                else if (lastReceivedPodPhase.equals(PodPhase.SUCCEEDED))
                {
                    stopDurationTimer();
                    metricReporter.report();
                }
            }
        }
    }
    private void startDurationTimer()
    {
        lastDurationContext = podDurationMetric.time();
    }

    private void stopDurationTimer()
    {
        if(lastDurationContext != null)
            lastDurationContext.stop();
    }

    @Override
    public void onClose(KubernetesClientException cause)
    {
        stopDurationTimer();
        metricReporter.report();
    }
}