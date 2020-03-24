package com.theplatform.dfh.cp.modules.kube.fabric8.client.facade;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.ConnectionTracker;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.PodResource;
import net.jodah.failsafe.Failsafe;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
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
    private static Logger logger = LoggerFactory.getLogger(RetryableKubernetesClient.class);

    private static final int DEFAULT_ATTEMPTS = 3;
    private static final int DEFAULT_DELAY_SECONDS = 2;
    private static final List<Class<? extends Throwable>> retryableExceptions = Arrays.asList(
        KubernetesClientException.class
    );

    private String name;
    private DefaultKubernetesClient kubernetesClient;
    private ConnectionTracker connectionTracker;

    public RetryableKubernetesClient(DefaultKubernetesClient kubernetesClient)
    {
        this(kubernetesClient, new ConnectionTracker());
    }

    public RetryableKubernetesClient(DefaultKubernetesClient kubernetesClient, ConnectionTracker connectionTracker)
    {
        super(DEFAULT_ATTEMPTS, DEFAULT_DELAY_SECONDS, retryableExceptions);
        this.kubernetesClient = kubernetesClient;
        this.connectionTracker = connectionTracker;
    }

    @Override
    public Pod startPod(Pod podToCreate)
    {
        return Failsafe.with(getRetryPolicy("Pod start attempt failed")).get(
            () -> kubernetesClient
                .pods()
                .create(podToCreate)
        );
    }

    @Override
    public void updatePodAnnotations(String podName, Map<String, String> annotations)
    {
        Failsafe.with(getRetryPolicy("Update pod annotations failed")).run(
            () ->
            {
                DoneablePod pod = kubernetesClient.pods().withName(podName).edit();
                pod.editMetadata().addToAnnotations(annotations).and().done();
            });
    }

    @Override
    public Map<String, String> getPodAnnotations(String podName)
    {
        return Failsafe.with(getRetryPolicy("Get pod annotations failed")).get(
            () ->
            {
                DoneablePod pod = kubernetesClient.pods().withName(podName).edit();
                return pod.buildMetadata().getAnnotations();
            });
    }

    @Override
    public PodResource<Pod, DoneablePod> getPodResource(String nameSpace, String podName)
    {
        return Failsafe.with(getRetryPolicy("Get pod resource failed")).get(
            () -> kubernetesClient.pods().inNamespace(nameSpace).withName(podName)
        );
    }

    @Override
    public Long getLastLogLineTimestamp(String namespace, String podName)
    {
        return Failsafe.with(getRetryPolicy("Get last log line timestamp failed")).get(
            () ->
            {
                String lastLogLine =
                    kubernetesClient.pods().inNamespace(namespace).withName(podName).usingTimestamps().tailingLines(1).getLog();
                if (lastLogLine != null && lastLogLine.length() > 0)
                {
                    return parseDatePrefix(lastLogLine);
                }
                return null;
            }
        );
    }

    protected Long parseDatePrefix(String message)
    {
        String reducedMessage = message;

        // Note: sometimes a tail call to a pod will return more than 1 line (very closely timed logging can cause this)
        // trim down the string if it's a multi line one
        if(message != null)
        {
            // may or may not end with a newline
            int startIdx = message.length() -
                (message.endsWith(System.lineSeparator()) ? 1 + System.lineSeparator().length() : 1);
            int subStringIndex = StringUtils.lastIndexOf(message, System.lineSeparator(), startIdx);
            if (subStringIndex > -1)
                reducedMessage = StringUtils.substring(message, subStringIndex + 1);
        }

        // RFC3339 Date format
        // example from the bravo kube cluster: 2019-08-26T23:19:30.381101127Z
        String[] messageParts = StringUtils.split(reducedMessage, ' ');
        if(messageParts != null && messageParts.length > 0)
        {
            try
            {
                return Instant.parse(messageParts[0]).toEpochMilli();
            }
            catch(DateTimeParseException e)
            {
                logger.error(String.format("Unable to parse timestamp: %1$s", messageParts[0]), e);
            }
        }
        return null;
    }

    @Override
    public void close()
    {
        kubernetesClient.close();
    }

    @Override
    public KubernetesClient getInternalClient()
    {
        return kubernetesClient;
    }

    @Override
    public ConnectionTracker getConnectionTracker()
    {
        return connectionTracker;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }
}
