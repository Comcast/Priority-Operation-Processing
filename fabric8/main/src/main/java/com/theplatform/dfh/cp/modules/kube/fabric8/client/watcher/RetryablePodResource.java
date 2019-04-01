package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Provides the basic functionality required by the PodWatcherImpl with wrapped up exception handling
 */
public class RetryablePodResource implements PodResourceFacade
{
    private PodResource<Pod, DoneablePod> podResource;
    private int delaySeconds = 1;
    private int attemptCount = 3;

    private static final List<Class<? extends Throwable>> retryableExceptions = Arrays.asList(
        KubernetesClientException.class
    );

    private RetryablePodResource(){}

    public RetryablePodResource(PodResource<Pod, DoneablePod> podResource)
    {
        this.podResource = podResource;
    }

    public Pod get()
    {
        return Failsafe.with(getRetryPolicy()).get(() -> podResource.get());
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

    private RetryPolicy<Object> getRetryPolicy()
    {
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
            .handle(retryableExceptions)
            .withMaxAttempts(attemptCount);
        if(delaySeconds > 0) retryPolicy.withDelay(Duration.ofSeconds(delaySeconds));
        return retryPolicy;
    }

    public void setDelaySeconds(int delaySeconds)
    {
        this.delaySeconds = delaySeconds;
    }

    public void setAttemptCount(int attemptCount)
    {
        this.attemptCount = attemptCount;
    }
}
