package com.theplatform.dfh.cp.modules.kube.fabric8.client.facade;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.PodResource;
import net.jodah.failsafe.Failsafe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provides the basic functionality for a KubernetesClient with retryable exception handling
 *
 * Observed a number of default-quota errors coming back from kubernetes
 * - https://github.com/kubernetes/kubernetes/issues/60988
 */

public class RetryableKubernetesClient extends RetryableBase implements KubernetesClientFacade
{
    private static final int DEFAULT_ATTEMPTS = 3;
    private static final int DEFAULT_DELAY_SECONDS = 1;
    private static final List<Class<? extends Throwable>> retryableExceptions = Arrays.asList(
        KubernetesClientException.class
    );

    private DefaultKubernetesClient kubernetesClient;

    public RetryableKubernetesClient(DefaultKubernetesClient kubernetesClient)
    {
        super(DEFAULT_ATTEMPTS, DEFAULT_DELAY_SECONDS, retryableExceptions);
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public Pod startPod(Pod podToCreate)
    {
        return Failsafe.with(getRetryPolicy()).get(
            () -> kubernetesClient
                .pods()
                .create(podToCreate)
        );
    }

    @Override
    public void updatePodAnnotations(String podName, Map<String, String> annotations)
    {
        DoneablePod pod = kubernetesClient.pods().withName(podName).edit();
        pod.editMetadata().addToAnnotations(annotations).and().done();
    }

    @Override
    public Map<String, String> getPodAnnotations(String podName)
    {
        DoneablePod pod = kubernetesClient.pods().withName(podName).edit();
        return pod.buildMetadata().getAnnotations();
    }

    @Override
    public PodResource<Pod, DoneablePod> getPodResource(String nameSpace, String fullName)
    {
        return kubernetesClient.pods().inNamespace(nameSpace).withName(fullName);
    }

    @Override
    public void close()
    {
        kubernetesClient.close();
    }
}
