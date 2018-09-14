package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.annotation.PodAnnotationClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class KubernetesLauncher implements BaseLauncher
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesLauncher.class);

    protected KubeConfig kubeConfig;
    protected PodConfig podConfig;
    protected ExecutionConfig executionConfig;
    protected PodPushClient podPushClient;
    protected PodFollower<PodPushClient> follower;

    public KubernetesLauncher(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        this.podPushClient = new PodPushClientFactoryImpl().getClient(kubeConfig);
        this.follower = new PodFollowerImpl<>(this.kubeConfig, podConfig, executionConfig);
    }

    private Consumer<String> getLineConsumer(final List<String> linesForProcessing)
    {
        return new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                linesForProcessing.add(s);
            }
        };
    }

    private Consumer<String> getConsumer(final StringBuilder stdoutCapture)
    {
        return new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                stdoutCapture.append(s).append("\n");
            }
        };
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
    public void execute(String payload)
    {
        logger.info("Launching Executor with Payload: {}", payload);

        executionConfig.getEnvVars().put(
            "PAYLOAD", payload
        );

        // Use this code to run in the mode that we will use in production.
        // don't follow the pod run, just kick it off and let it be.
//        podConfig.setReapCompletedPods(true);
//        podPushClient.startWithoutWatcher(podConfig, executionConfig);


        // Use this code to actually follow the pod
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

            lastPodPhase = follower.startAndFollowPod(logLineObserver);

            logger.info("Executor completed with pod status {}", lastPodPhase.phase.getLabel());
            if (lastPodPhase.phase.hasFinished())
            {
                if (lastPodPhase.phase.isFailed())
                {
                    logger.error("Executor failed to produce metadata, output was : {}", allStdout);
                    throw new RuntimeException(allStdout.toString());
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Executor produced: {}", allStdout.toString());
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
    }
}