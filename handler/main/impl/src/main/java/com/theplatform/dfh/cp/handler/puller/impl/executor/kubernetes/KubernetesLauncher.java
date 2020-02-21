package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaInsight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodFollowerFactory;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactory;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class KubernetesLauncher implements BaseLauncher
{
    private static final String AGENDA_TYPE = "exec"; // todo constant pending implementation of agenda types
    private static final String PULLER_AGENDA_METADATA_PATTERN = " Puller Agenda metadata: agendaType=%s owner=%s agendaId=%s ";
    protected Logger logger = LoggerFactory.getLogger(KubernetesLauncher.class);

    protected PodPushClientFactory<CpuRequestModulator> podPushClientFactory;
    protected PodFollowerFactory<PodPushClient> podFollowerFactory;
    protected KubeConfig kubeConfig;
    protected PodConfig podConfig;
    protected ExecutionConfig executionConfig;
    protected PodPushClient podPushClient;
    protected PodFollower<PodPushClient> follower;
    protected JsonHelper jsonHelper;
    protected PullerLaunchDataWrapper launchDataWrapper;

    public KubernetesLauncher(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig, PullerLaunchDataWrapper launchDataWrapper)
    {
        this(kubeConfig, podConfig, executionConfig, launchDataWrapper, new PodPushClientFactoryImpl(), new PodFollowerFactory<>());
    }

    public KubernetesLauncher(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig, PullerLaunchDataWrapper launchDataWrapper,
        PodPushClientFactory<CpuRequestModulator> podPushClientFactory, PodFollowerFactory<PodPushClient> podFollowerFactory)
    {
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        this.launchDataWrapper = launchDataWrapper;
        this.podPushClientFactory = podPushClientFactory;
        this.podFollowerFactory = podFollowerFactory;
        this.podPushClient = podPushClientFactory.getClient(kubeConfig);
        this.follower = podFollowerFactory.createPodFollower(this.kubeConfig, podConfig, executionConfig);
        this.jsonHelper = new JsonHelper();
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

    /**
     *  Use this code to run in the mode that we will use in production.
     *  Don't follow the pod run, just kick it asynchronously.
     * @param agenda The agenda to execute
     * @param agendaProgress (optional) AgendaProgress to pass along (this is for the retry case where some ops have executed)
     */
    @Override
    public void execute(Agenda agenda, AgendaProgress agendaProgress)
    {
        logAgendaMetadata(agenda);
        ExecutionAgendaConfigurator executionConfigurator = new ExecutionAgendaConfigurator(executionConfig, jsonHelper, launchDataWrapper.getPayloadWriterFactory().createWriter());
        executionConfigurator.setEnvVars(agenda, agendaProgress);
        podConfig.setReapCompletedPods(true);
        appendLabels(podConfig, agenda);
        podPushClient.startWithoutWatcher(podConfig, executionConfig);
        followPod();
    }

    protected void appendLabels(PodConfig podConfig, Agenda agenda)
    {
        Map<String, String> labels = new HashMap<>();
        AgendaInsight agendaInsight = agenda.getAgendaInsight();
        if(agendaInsight != null &&
            agendaInsight.getInsightId() != null
            && agendaInsight.getResourcePoolId() != null)
        {
            labels.put(KubernetesFissionConstants.EXECEUTOR_INSIGHT_LABEL, agendaInsight.getInsightId());
            labels.put(KubernetesFissionConstants.EXECEUTOR_RESOURCEPOOL_LABEL, agendaInsight.getResourcePoolId());
        }
        podConfig.setLabels(labels);
    }

    protected void logAgendaMetadata(Agenda agenda)
    {
        String owner = "agenda owner not visible";
        owner = agenda != null && agenda.getCustomerId() != null ? agenda.getCustomerId() : owner;
        String agendId = "agendaId not visible";
        agendId = agenda != null && agenda.getId() != null ? agenda.getId() : agendId;
        logger.info(String.format(PULLER_AGENDA_METADATA_PATTERN, AGENDA_TYPE, owner, agendId));
    }


    // TODO consider if there is diagnostic value in conditionally enabling this logic or if it could be deleted.
    @Deprecated
    private void followPod()
    {
//        // Use this code to actually follow the pod
//        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);
//
//        logger.info("Getting progress until the pod {} is finished.", executionConfig.getName());
//        StringBuilder allStdout = new StringBuilder();
//        logLineObserver.addConsumer(new Consumer<String>()
//        {
//            @Override
//            public void accept(String s)
//            {
//                logger.info("STDOUT: {}", s);
//            }
//        });
//        FinalPodPhaseInfo lastPodPhase = null;
//        try
//        {
//            logger.info("Starting the pod with name {}", executionConfig.getName());
//
//            lastPodPhase = follower.startAndFollowPod(logLineObserver);
//
//            logger.info("Executor completed with pod status {}", lastPodPhase.phase.getLabel());
//            if (lastPodPhase.phase.hasFinished())
//            {
//                if (lastPodPhase.phase.isFailed())
//                {
//                    logger.error("Executor failed to produce metadata, output was : {}", allStdout);
//                    throw new RuntimeException(allStdout.toString());
//                }
//            }
//
//            if (logger.isDebugEnabled())
//            {
//                logger.debug("Executor produced: {}", allStdout.toString());
//            }
//        }
//        catch (Exception e)
//        {
//            String allStringMetadata = allStdout.toString();
//            logger.error("Exception caught {}", allStringMetadata, e);
//            throw new RuntimeException(allStringMetadata, e);
//        }
//        logger.info("Done with execution of pod: {}", executionConfig.getName());
    }
}