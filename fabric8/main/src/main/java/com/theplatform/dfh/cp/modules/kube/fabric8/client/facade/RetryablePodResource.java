package com.theplatform.dfh.cp.modules.kube.fabric8.client.facade;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcherImpl;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import net.jodah.failsafe.Failsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Provides the basic functionality for a PodResource with retryable exception handling
 */
public class RetryablePodResource extends RetryableBase implements PodResourceFacade
{
    private static final int DEFAULT_ATTEMPTS = 4;
    private static final int DEFAULT_DELAY_SECONDS = 2;
    private static final boolean USE_BACKOFF = true;
    private static final List<Class<? extends Throwable>> retryableExceptions = Arrays.asList(
        KubernetesClientException.class
    );

    private PodResource<Pod, DoneablePod> podResource;

    public RetryablePodResource(PodResource<Pod, DoneablePod> podResource)
    {
        super(DEFAULT_ATTEMPTS, DEFAULT_DELAY_SECONDS, USE_BACKOFF, retryableExceptions);
        this.podResource = podResource;
    }

    public Pod get()
    {
        return Failsafe.with(getRetryPolicy("Pod get attempt failed.")).get(() -> podResource.get());
    }

    public String getLog()
    {
        return podResource.getLog();
    }

    public Watch watch(PodWatcherImpl podWatcherImpl)
    {
        return podResource.watch(podWatcherImpl);
    }

    public LogWatch watchLog()
    {
        return podResource.watchLog();
    }
}
