package com.theplatform.dfh.cp.handler.executor.impl.executor.factory;

import com.theplatform.dfh.cp.handler.executor.impl.executor.KubernetesAgendaExecutor;
import com.theplatform.dfh.cp.handler.base.perform.Executor;
import com.theplatform.dfh.cp.handler.base.perform.RetryableExecutor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.filehandler.k8.PodFollower;
import com.theplatform.dfh.filehandler.k8.PodFollowerConfig;
import com.theplatform.dfh.filehandler.k8.PodPushClient;
import com.theplatform.dfh.filehandler.k8.config.KubeConfig;
import com.theplatform.dfh.filehandler.k8.factory.PodClientFactory;
import com.theplatform.dfh.filehandler.k8.modulation.CpuRequestModulation;
import com.theplatform.dfh.filehandler.k8.modulation.CpuRequestModulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through kubernetes)
 */
public class KubernetesAgendaExecutorFactory implements AgendaExecutorFactory
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesAgendaExecutorFactory.class);

    private KubeConfig kubeConfig;
    private PodClientFactory<CpuRequestModulator> podClientFactory;
    private int podRetryDelay = 2000;

    public KubeConfig getKubeConfig()
    {
        return kubeConfig;
    }

    public void setKubeConfig(KubeConfig kubeConfig)
    {
        this.kubeConfig = kubeConfig;
    }

    public PodClientFactory<CpuRequestModulator> getPodClientFactory()
    {
        return podClientFactory;
    }

    public void setPodClientFactory(PodClientFactory<CpuRequestModulator> podClientFactory)
    {
        this.podClientFactory = podClientFactory;
    }

    @Override
    public Executor<List<String>> getMediaInfoExecutor(String filePath, LaunchDataWrapper launchDataWrapper)
    {
        String dockerImageName = launchDataWrapper.getPropertyRetriever().getField("dockerImageName");
        PodFollower<PodPushClient> follower = podClientFactory.getPodFollower();
        PodFollowerConfig config = new PodFollowerConfig();
        follower.withFollowerConfig(config);
        PodPushClient wrapperClient = podClientFactory
            .getClient(kubeConfig, kubeConfig.getPodRetryCount(), TimeUnit.MILLISECONDS, podRetryDelay);
        wrapperClient.setCpuModulator(new CpuRequestModulator()
        {
            @Override
            public void setModulation(CpuRequestModulation cpuRequestModulation)
            {

            }

            @Override
            public String getCpuRequest()
            {
                return kubeConfig.getCpuMinimumRequestCount();
            }
        });

        KubernetesAgendaExecutor mediaInfoExecutor = new KubernetesAgendaExecutor(filePath);
        mediaInfoExecutor.setFollower(follower);
        mediaInfoExecutor.setClient(wrapperClient);
        mediaInfoExecutor.setDockerImageName(dockerImageName);
        return new RetryableExecutor<>(mediaInfoExecutor, 0/*kubeConfig.getPodRetryCount()*/);
    }
}