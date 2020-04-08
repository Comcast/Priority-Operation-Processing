package com.comcast.pop.handler.sample.impl.executor.kubernetes;

import com.comcast.pop.handler.sample.impl.exception.SampleHandlerException;
import com.comcast.pop.handler.sample.impl.executor.BaseExternalExecutor;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.client.logging.LogLineObserver;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollower;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.comcast.pop.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class KubernetesExternalExecutor extends BaseExternalExecutor
{
    protected KubeConfig kubeConfig;
    protected PodConfig podConfig;
    protected ExecutionConfig executionConfig;
    protected PodFollower<PodPushClient> follower;

    public KubernetesExternalExecutor(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        super();
        this.kubeConfig = kubeConfig;

        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
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

    public void setFollower(PodFollower<PodPushClient> follower)
    {
        this.follower = follower;
    }

    @Override
    public List<String> execute()
    {
        logger.debug("Executing external {}", new JsonHelper().getJSONString(podConfig));

        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);

        logger.info("Getting progress until the pod {} is finished.", executionConfig.getName());
        StringBuilder allStdout = new StringBuilder();
        List<String> linesForProcessing = new LinkedList<>();
        logLineObserver.addConsumer(getConsumer(allStdout));
        logLineObserver.addConsumer(getLineConsumer(linesForProcessing));

        FinalPodPhaseInfo finalPodPhaseInfo = null;
        try
        {
            logger.info("Starting the pod with name {}", executionConfig.getName());

            finalPodPhaseInfo = follower.startAndFollowPod(logLineObserver);

            logger.info("External execution completed with pod status {}", finalPodPhaseInfo.phase.getLabel());
            if (finalPodPhaseInfo.phase.hasFinished())
            {
                if (finalPodPhaseInfo.phase.isFailed())
                {
                    logger.error("External execution failed to produce metadata, output was : {}", allStdout);
                    throw new SampleHandlerException(allStdout.toString());
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("External execution produced: {}", allStdout.toString());
            }
        }
        catch (Exception e)
        {
            String allStringMetadata = allStdout.toString();
            logger.error("Exception caught {}", allStringMetadata, e);
            throw new SampleHandlerException("Failed to execute pod. Output from execution: " + allStringMetadata, e);
        }
        return linesForProcessing;
    }
}