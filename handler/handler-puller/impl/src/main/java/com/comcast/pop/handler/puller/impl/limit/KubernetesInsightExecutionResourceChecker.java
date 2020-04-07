package com.comcast.pop.handler.puller.impl.limit;

import com.comcast.pop.handler.puller.impl.executor.kubernetes.KubernetesFissionConstants;
import com.comcast.pop.handler.puller.impl.executor.kubernetes.KubernetesLauncher;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KubernetesInsightExecutionResourceChecker implements ResourceChecker
{
    public static final String INSIGHT_EXECUTION_LIMIT_PROPERTY_NAME = "insight.execution.limit";

    private static final Set<String> EXECUTING_POD_STATUSES = new HashSet<>(Arrays.asList(
        PodPhase.PENDING.getLabel(), PodPhase.RUNNING.getLabel(), PodPhase.UNKNOWN.getLabel()
    ));

    private static final Logger logger = LoggerFactory.getLogger(KubernetesLauncher.class);
    private KubernetesClient kubernetesClient;
    private String insightId;
    private int executionLimit = 0;

    public KubernetesInsightExecutionResourceChecker()
    {
    }

    public KubernetesInsightExecutionResourceChecker(KubernetesClient kubernetesClient, String insightId, int executionLimit)
    {
        this.kubernetesClient = kubernetesClient;
        this.insightId = insightId;
        this.executionLimit = executionLimit;
    }

    @Override
    public boolean areResourcesAvailable()
    {
        try
        {
            List<Pod> executorsWithInsight = kubernetesClient.pods()
                .inNamespace(kubernetesClient.getNamespace())
                .withLabel(KubernetesFissionConstants.EXECEUTOR_INSIGHT_LABEL, insightId)
                .list().getItems();

            List<Pod> runningExecutors = executorsWithInsight.stream().filter(pod ->
                pod.getStatus() != null && EXECUTING_POD_STATUSES.contains(pod.getStatus().getPhase()))
                .collect(Collectors.toList());
            logger.info("{}/{} Executors running insight: {}", runningExecutors.size(), executionLimit, insightId);
            return runningExecutors.size() < executionLimit;
        }
        catch(Exception e)
        {
            logger.error(String.format("Failed to retrieve executing pods with insight: %1$s", insightId), e);
            // cannot accurately indicate so just default
            return false;
        }
    }

    public KubernetesClient getKubernetesClient()
    {
        return kubernetesClient;
    }

    public KubernetesInsightExecutionResourceChecker setKubernetesClient(KubernetesClient kubernetesClient)
    {
        this.kubernetesClient = kubernetesClient;
        return this;
    }

    public String getInsightId()
    {
        return insightId;
    }

    public KubernetesInsightExecutionResourceChecker setInsightId(String insightId)
    {
        this.insightId = insightId;
        return this;
    }

    public int getExecutionLimit()
    {
        return executionLimit;
    }

    public KubernetesInsightExecutionResourceChecker setExecutionLimit(int executionLimit)
    {
        this.executionLimit = executionLimit;
        return this;
    }
}
