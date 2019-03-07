package com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.OperationProgressProvider;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Executor for launching the necessary components via kubernetes to complete the operation
 */
public class KubernetesOperationExecutor extends BaseOperationExecutor
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutor.class);

    protected KubeConfig kubeConfig;
    protected PodConfig podConfig;
    protected ExecutionConfig executionConfig;
    protected PodFollower<PodPushClient> follower;
    protected JsonHelper jsonHelper;

    public KubernetesOperationExecutor(Operation operation, KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig, LaunchDataWrapper launchDataWrapper)
    {
        super(operation, launchDataWrapper);
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        this.jsonHelper = new JsonHelper();
        this.follower = new PodFollowerImpl<>(this.kubeConfig, this.podConfig, this.executionConfig);
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
    public OperationProgress retrieveOperationProgress()
    {
        try
        {
            Map<String,String> podAnnotations = follower.getPodAnnotations();
            String progressJson = podAnnotations.getOrDefault(KubernetesReporter.REPORT_PROGRESS_ANNOTATION, null);
            String resultPayload = podAnnotations.get(KubernetesReporter.REPORT_PAYLOAD_ANNOTATION);
            if(progressJson == null) return null;
            OperationProgress operationProgress = jsonHelper.getObjectFromString(progressJson, OperationProgress.class);
            operationProgress.setOperation(operation.getName());
            operationProgress.setResultPayload(resultPayload);
            return operationProgress;
        }
        catch(JsonHelperException je)
        {
            logger.error("{} - Unable to convert progress string to OperationProgress", operation.getName());
        }
        catch(Exception e)
        {
            logger.error("{} - Unable to pull pod annotation: {}", operation.getName(), KubernetesReporter.REPORT_PROGRESS_ANNOTATION);
        }
        return null;
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);

        configureMetadata(payload);

        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);

        logger.info("Getting progress until the pod {} is finished.", executionConfig.getName());
        StringBuilder allStdout = new StringBuilder();
        logLineObserver.addConsumer(new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                // TEMP commented out... probably don't want this in the long term
                logger.info("STDOUT: {}", s);
            }
        });
        FinalPodPhaseInfo lastPodPhase = null;
        try
        {
            logger.info("Starting the pod with name {}", executionConfig.getName());

            lastPodPhase = follower.startAndFollowPod(logLineObserver);

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

        Map<String,String> podAnnotations = follower.getPodAnnotations();
        return podAnnotations.get(KubernetesReporter.REPORT_PAYLOAD_ANNOTATION);
    }

    private void configureMetadata(String payload)
    {
        Map<String,String> envVars = executionConfig.getEnvVars();
        envVars.put(HandlerField.PAYLOAD.name(), payload);

        setEnvVar(envVars, HandlerField.CID);
        setEnvVar(envVars, HandlerField.CUSTOMER_ID);

        if(operation.getName() != null) envVars.put(HandlerField.OPERATION_NAME.name(), operation.getName());
        if(operation.getId() != null) envVars.put(HandlerField.OPERATION_ID.name(), operation.getId());
    }

    private void setEnvVar(Map<String,String> envVars, HandlerField handlerField)
    {
        String handlerFieldValue = launchDataWrapper.getEnvironmentRetriever().getField(handlerField.name(), null);
        if(handlerFieldValue != null) envVars.put(handlerField.name(), handlerFieldValue);
    }
}