package com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.messages.ExecutorMessages;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
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
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Executor for launching the necessary components via kubernetes to complete the operation
 */
public class KubernetesOperationExecutor extends BaseOperationExecutor
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutor.class);
    public static final int MAX_POD_LOG_LINES = 1024;
    public static final int DEFAULT_FAIL_DIAGNOSTIC_LINE_LIMIT = 10;

    private KubeConfig kubeConfig;
    private PodConfig podConfig;
    private ExecutionConfig executionConfig;
    private PodFollower<PodPushClient> follower;
    private JsonHelper jsonHelper;
    private OperationProgress defaultFailedOperationProgress;

    public KubernetesOperationExecutor(Operation operation, KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig, LaunchDataWrapper launchDataWrapper)
    {
        super(operation, launchDataWrapper);
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        this.jsonHelper = new JsonHelper();
        this.follower = new PodFollowerImpl<>(this.kubeConfig, this.podConfig, this.executionConfig);
        setIdenitifier(executionConfig.getName());
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
            logger.debug("[{}] Getting Pod annotations", executionConfig.getName());
            Map<String,String> podAnnotations = follower.getPodAnnotations();
            String progressJson = podAnnotations.getOrDefault(KubernetesReporter.REPORT_PROGRESS_ANNOTATION, null);
            String resultPayload = podAnnotations.get(KubernetesReporter.REPORT_PAYLOAD_ANNOTATION);
            logger.debug("progressJson [" + progressJson + "], resultPayload [" + resultPayload + ']');
            if(progressJson == null)
            {
                return defaultFailedOperationProgress;
            }
            logger.debug("[{}] Getting operationProgress", executionConfig.getName());
            OperationProgress operationProgress = jsonHelper.getObjectFromString(progressJson, OperationProgress.class);
            logger.debug("[{}] OperationProgress [{}]",  executionConfig.getName(), operationProgress.getOperation());
            operationProgress.setOperation(operation.getName());
            operationProgress.setResultPayload(resultPayload);
            return operationProgress;
        }
        catch(JsonHelperException je)
        {
            logger.error("[{}] {} - Unable to convert progress string to OperationProgress", executionConfig.getName(), operation.getName());
        }
        catch(Exception e)
        {
            logger.error("[{}] {} - Unable to pull pod annotation: {}",
                executionConfig.getName(), operation.getName(), KubernetesReporter.REPORT_PROGRESS_ANNOTATION);
        }
        return null;
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);

        CircularFifoQueue<String> podLogOutputQueue = new CircularFifoQueue<>(MAX_POD_LOG_LINES);

        configureMetadata(payload);

        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);

        logger.info("[{}] Getting progress until the pod is finished.", executionConfig.getName());
        logLineObserver.addConsumer(new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                podLogOutputQueue.add(s);
                logger.trace("STDOUT: {}", s);
            }
        });
        FinalPodPhaseInfo lastPodPhase;
        String errorMessage = null;
        Exception exception = null;
        try
        {
            logger.info("Starting the pod with name {}", executionConfig.getName());
            lastPodPhase = follower.startAndFollowPod(logLineObserver);
            logger.info("[{}]{} completed with pod status {}", executionConfig.getName(), operation.getName(), lastPodPhase.phase.getLabel());
            if (lastPodPhase.phase.hasFinished())
            {
                if (lastPodPhase.phase.isFailed())
                {
                    errorMessage = ExecutorMessages.KUBERNETES_POD_FAILED.getMessage(executionConfig.getName());
                }
            }
        }
        catch (Exception e)
        {
            errorMessage = ExecutorMessages.KUBERNETES_FOLLOW_ERROR.getMessage(executionConfig.getName());
            exception = e;
        }

        if(errorMessage != null)
        {
            // setup the default failed progress just in case the pod annotations have nothing of use (no op progress payload)
            defaultFailedOperationProgress = generateFailedOperationProgress(operation.getName(), errorMessage, exception, podLogOutputQueue);
            throw new RuntimeException(errorMessage, exception);
        }

        logger.info("[{}] Pod execution complete", executionConfig.getName());

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

    protected static OperationProgress generateFailedOperationProgress(String operationName, String diagnosticMessage,
        Throwable t, CircularFifoQueue<String> podLogOutput)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setOperation(operationName);
        operationProgress.setProcessingState(ProcessingState.COMPLETE);
        operationProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());

        List<String> strings = IntStream.range(Math.max(0, podLogOutput.size() - DEFAULT_FAIL_DIAGNOSTIC_LINE_LIMIT), podLogOutput.size())
            .mapToObj(podLogOutput::get).collect(Collectors.toList());
        operationProgress.setDiagnosticEvents(new DiagnosticEvent[]
            {
                new DiagnosticEvent(
                    diagnosticMessage,
                    t,
                    strings)
            });
        return operationProgress;
    }
}