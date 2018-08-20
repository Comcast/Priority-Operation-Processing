package com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.annotation.PodAnnotationClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class KubernetesOperationExecutor extends BaseOperationExecutor
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutor.class);

    protected KubeConfig kubeConfig;
    protected PodConfig podConfig;
    protected ExecutionConfig executionConfig;
    protected PodFollower<PodPushClient> follower;

    public KubernetesOperationExecutor(Operation operation, KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        super(operation);
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        this.follower = new PodFollowerImpl<>(this.kubeConfig);
    }

    public void setFollower(PodFollower<PodPushClient> follower)
    {
        this.follower = follower;
    }

    public void setKubeConfig(KubeConfig kubeConfig)
    {
        this.kubeConfig = kubeConfig;
    }

    public void setPodConfig(PodConfig podConfig)
    {
        this.podConfig = podConfig;
    }

    public void setExecutionConfig(ExecutionConfig executionConfig)
    {
        this.executionConfig = executionConfig;
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);

        executionConfig.getEnvVars().put("PAYLOAD", payload);

        // TODO: isn't this already happening somewhere?
        executionConfig.setName(podConfig.getNamePrefix() + UUID.randomUUID().toString());

        // TODO!! we need to get the annotations before reaping... probably should build that into the follower
        podConfig.setReapCompletedPods(false);

        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);

        logger.info("Getting progress until the pod {} is finished.", executionConfig.getName());
        StringBuilder allStdout = new StringBuilder();
        logLineObserver.addConsumer(new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                logger.info("STDOUT: {}", s);
            }
        });
        FinalPodPhaseInfo lastPodPhase = null;
        try
        {
            logger.info("Starting the pod with name {}", executionConfig.getName());

            lastPodPhase = follower.startAndFollowPod(podConfig, executionConfig, logLineObserver);

            logger.info("{} completed with pod status {}", operation.getName(), lastPodPhase.phase.getLabel());
            if (lastPodPhase.phase.hasFinished())
            {
                if (lastPodPhase.phase.isFailed())
                {
                    logger.error("{} failed to produce metadata, output was : {}", operation.getName(), allStdout);
                    throw new RuntimeException(allStdout.toString());
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("{} produced: {}", operation.getName(), allStdout.toString());
            }
        }
        catch (Exception e)
        {
            String allStringMetadata = allStdout.toString();
            logger.error("Exception caught {}", allStringMetadata, e);
            throw new RuntimeException(allStringMetadata, e);
        }
        logger.info("Done with execution of pod: {}", executionConfig.getName());

        Map<String,String> podAnnotations = new PodAnnotationClient(new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig)), executionConfig.getName())
            .getPodAnnotations();
        String result = podAnnotations.get(KubernetesReporter.REPORT_SUCCESS_ANNOTATION);

        follower.getPodPushClient().deletePod(executionConfig.getName());

        return result;
    }
}