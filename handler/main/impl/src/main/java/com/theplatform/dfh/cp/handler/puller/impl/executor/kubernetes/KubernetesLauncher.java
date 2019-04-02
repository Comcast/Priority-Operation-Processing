package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class KubernetesLauncher implements BaseLauncher
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesLauncher.class);

    protected KubeConfig kubeConfig;
    protected PodConfig podConfig;
    protected ExecutionConfig executionConfig;
    protected PodPushClient podPushClient;
    protected PodFollower<PodPushClient> follower;
    protected JsonHelper jsonHelper;

    public KubernetesLauncher(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        this.podPushClient = new PodPushClientFactoryImpl().getClient(kubeConfig);
        this.follower = new PodFollowerImpl<>(this.kubeConfig, podConfig, executionConfig);
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
     * @param agenda
     */
    @Override
    public void execute(Agenda agenda)
    {
        extractEnvVars(agenda);
        podConfig.setReapCompletedPods(true);
        podPushClient.startWithoutWatcher(podConfig, executionConfig);
        followPod();
    }

    private void extractEnvVars(Agenda agenda)
    {
        String payload = jsonHelper.getJSONString(agenda);
        logger.info("Launching Executor with Payload: {}", payload);

        extractEnvVar(HandlerField.PAYLOAD.name(), payload);
        extractEnvVar(HandlerField.CID.name(), getParam(agenda.getParams(), GeneralParamKey.cid));
        extractEnvVar(HandlerField.AGENDA_ID.name(), agenda.getId());
        extractEnvVar(HandlerField.CUSTOMER_ID.name(), getParam(agenda.getParams(), GeneralParamKey.customerId));
        extractEnvVar(HandlerField.PROGRESS_ID.name(), agenda.getProgressId());
    }

    private void extractEnvVar(String key, String value)
    {
        if(!StringUtils.isBlank(value))
        {
            executionConfig.getEnvVars().put(key, value);
        }
        else
        {
            logger.warn("No " + key + " was set on the Agenda.");
        }
    }

    private String getParam(ParamsMap paramsMap, ParamKey key)
    {
        return paramsMap == null ? null : paramsMap.getString(key);
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