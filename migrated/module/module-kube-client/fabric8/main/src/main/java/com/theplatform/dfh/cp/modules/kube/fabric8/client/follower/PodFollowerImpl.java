package com.theplatform.dfh.cp.modules.kube.fabric8.client.follower;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.annotation.PodAnnotationClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.exception.PodException;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.exception.PodNotScheduledException;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineObserverImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.SimpleLogLineSubscriber;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodEventListener;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Convenient tool for starting off a pod and following it until it has completed.
 */
public class PodFollowerImpl<C extends PodPushClient> implements PodFollower<C>
{
    public static final int LATCH_TIMEOUT = 1000;
    public static final int MAX_INACTIVITY_BEFORE_LOG_RESET = 10;
    private static Logger logger = LoggerFactory.getLogger(PodFollowerImpl.class);

    private PodPushClientFactoryImpl podPushClientFactory = new PodPushClientFactoryImpl();
    private PodPushClient podPushClient;

    private PodAnnotationClient podAnnotationClient;
    private Map<String, String> podAnnotations;

    private KubeConfig kubeConfig;
    private PodConfig podConfig;
    private ExecutionConfig executionConfig;
    private List<PodEventListener> eventListeners = new ArrayList<>();
    private int logActivityResetCounter = 0;

    public PodFollowerImpl(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        podPushClient = podPushClientFactory.getClient(kubeConfig);
        podPushClient.getKubernetesHttpClients().updatePodName(executionConfig.getName());
        podAnnotationClient = new PodAnnotationClient(podPushClient.getKubernetesHttpClients().getRequestClient(), executionConfig.getName());
    }

    public PodFollowerImpl(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig, PodPushClientFactoryImpl podPushClientFactory)
    {
        this.kubeConfig = kubeConfig;
        this.podConfig = podConfig;
        this.executionConfig = executionConfig;
        this.podPushClientFactory = podPushClientFactory;
        podPushClient = podPushClientFactory.getClient(kubeConfig);
        podPushClient.getKubernetesHttpClients().updatePodName(executionConfig.getName());
        podAnnotationClient = new PodAnnotationClient(podPushClient.getKubernetesHttpClients().getRequestClient(), executionConfig.getName());
    }

    public PodFollowerImpl addEventListener(PodEventListener listener)
    {
        this.eventListeners.add(listener);
        return this;
    }

    /**
     * Synchronous "blocking" call.
     * Create the Pod and don't return until it's finished executing. If podConfig.reapCompletedPods is true,
     * Pod will be deleted.
     *
     * NOTE: This method should only be called once.  There will be strange issues if it's called more than once.
     *
     * @param logLineObserver a logLine observer that will be attached to the stdout stream of the pod started.
     * @return the LastPhase (success or failure) from the pod being followed.
     */
    @Override
    public FinalPodPhaseInfo startAndFollowPod(LogLineObserver logLineObserver)
    {
        String podName = executionConfig.getName();
        podAnnotationClient.setPodName(podName);
        PodWatcher podWatcher = null;
        FinalPodPhaseInfo finalPhase = null;
        try
        {
            CountDownLatch podScheduled = new CountDownLatch(1);
            CountDownLatch podFinishedSuccessOrFailure = new CountDownLatch(1);

            // START POD
            logger.debug("K8s URL [" + executionConfig.getName() + "], image [" + podConfig.getImageName() +
                "], service account [" + podConfig.getServiceAccountName() + "]");

            podWatcher = podPushClient
                .start(podConfig, executionConfig, podScheduled, podFinishedSuccessOrFailure);

            podWatcher.addEventListeners(eventListeners);
            // CHECK POD: SCHEDULED
            // Doesn't need the podWatcher, since we have a latch to wait on.
            schedulePodCheck(podConfig, podName, podScheduled);
            // CHECK POD: SUCCESS/FAILURE
            keepCheckForFinishedState(podConfig, executionConfig, logLineObserver, podFinishedSuccessOrFailure, podWatcher);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred for podName {}", podName, e);
            throw new PodException(e);
        }
        finally
        {
            try
            {
                logLineObserver.done();
            }
            catch (Exception e)
            {
                logger.error("log line observer failed to compelete", e);
            }

            try
            {
                podAnnotations = podAnnotationClient.getPodAnnotations();
            }
            catch (Exception e)
            {
                logger.error("Failure getting pod annotations", e);
            }

            try
            {
                if (podWatcher != null)
                {
                    finalPhase = podWatcher.getFinalPodPhaseInfo();
                }
                if (podName != null && podConfig.getReapCompletedPods())
                {
                    logger.debug("Pod {} reaping enabled.", podName);
                    logger.info("Pod {} being removed with with pod phase {}", podName, finalPhase);
                    podPushClient.deletePod(podName);
                }
            }
            catch (Exception e)
            {
                logger.error("Failure during follower completion.", e);
            }

            try
            {
                podPushClient.close();
            }
            catch (Exception e)
            {
                logger.error("Fail to close PodPushClient.", e);
            }
        }
        return finalPhase;
    }

    private void schedulePodCheck(PodConfig podConfig, String podName, CountDownLatch podScheduled)
    {
        try
        {
            keepCheckingForRunningState(podConfig, podName, podScheduled);
        }
        catch (Exception e)
        {
            logger.error("Exception occured for podName {}", podName, e);
            throw new PodNotScheduledException(e);
        }
    }

    private void keepCheckingForRunningState(PodConfig podConfig, String podName, CountDownLatch podScheduled)
        throws InterruptedException, TimeoutException
    {
        ResetableTimeout resetableScheduleTimeout = new ResetableTimeout(
            podConfig.getPodScheduledTimeoutMs());
        boolean isScheduled = false;
        do
        {
            isScheduled = podScheduled.await(LATCH_TIMEOUT, TimeUnit.MILLISECONDS);
            logger.debug("Waiting for pod {} to schedule and run.", podName);
            resetableScheduleTimeout.timeout(podName);
        }
        while (!isScheduled);
        logger.debug("Pod {} passed scheduling watch.", podName);
    }

    private void keepCheckForFinishedState(PodConfig podConfig, ExecutionConfig executionConfig, LogLineObserver logLineObserver,
        CountDownLatch podFinishedSuccessOrFailure, PodWatcher podWatcher)
        throws InterruptedException, TimeoutException
    {
        boolean isFinished = false;
        do
        {
            isFinished = runTillFinished(
                logLineObserver, executionConfig.getName(), podFinishedSuccessOrFailure, executionConfig.getLogLineAccumulator(), podWatcher);
        }
        while (!isFinished);
    }

    private boolean runTillFinished(LogLineObserver logLineObserver, String podName,
        CountDownLatch podFinishedSuccessOrFailure, LogLineAccumulator logLineAccumulator, PodWatcher podWatcher) throws InterruptedException, TimeoutException
    {
        boolean isFinished;
        isFinished = podFinishedSuccessOrFailure.await(LATCH_TIMEOUT, TimeUnit.MILLISECONDS);
        logger.trace("Waiting for end state of pod {}", podName);
        List<String> linesProduced = logLineAccumulator.takeAll();
        int size = linesProduced.size();
        if (size > 0)
        {
            // incoming logs != new logs (this is because a log watch will pickup all the old logs when reconnected)
            // the checkLogTimeout below uses the PodStdoutTimeout to check for log issues just before a log reset is triggered
            logger.debug("Lines produced for [{}] : {}", podName, size);
            logLineObserver.send(linesProduced);
            logActivityResetCounter = 0;
        }
        else
        {
            logActivityResetCounter++;
        }

        logThisPodsLogs(linesProduced, podName);
        if(logActivityResetCounter > 0)
        {
            logger.info("[{}]Log inactivity detected - Next reset ({}/{})", podName, logActivityResetCounter, MAX_INACTIVITY_BEFORE_LOG_RESET);
        }
        if(logActivityResetCounter >= MAX_INACTIVITY_BEFORE_LOG_RESET)
        {
            // before resetting the log connection check if the overall log timeout has been exceeded
            if(checkLogTimeout(podConfig.getPodStdoutTimeout(), kubeConfig.getNameSpace(), podName))
            {
                // logs have timed out completely
                throw new TimeoutException(
                    String.format("[%1$s]Stdout log tail indicates no log activity for over %2$sms", podName, podConfig.getPodStdoutTimeout()));
            }
            logger.warn("[{}]Noticing inactivity on logging system. Resetting log connection.", podName);
            logActivityResetCounter = 0;
            podWatcher.resetLogging();
        }
        return isFinished;
    }

    /**
     * Checks if the stdout logs have not changed on the pod within the specified timeout
     * @return true if the log has timed out, false otherwise
     */
    protected boolean checkLogTimeout(Long timeout, String namespace, String podName)
    {
        if(timeout == null) return false;

        Long tailTimestamp = podPushClient.getKubernetesHttpClients().getRequestClient().getLastLogLineTimestamp(namespace, podName);
        Long now = Instant.now().toEpochMilli();
        if(tailTimestamp != null)
        {
            if (timeout < now - tailTimestamp)
            {
                logger.error("[{}]Pod has timed out. tailTimestamp/Now: {}/{}", podName, tailTimestamp, now);
                return true;
            }
            else
            {
                logger.error("[{}]No log activity on pod detected. Will timeout after approximately: {}ms", podName, timeout - (now - tailTimestamp));
            }
        }
        return false;
    }

    protected void logThisPodsLogs(List<String> linesProduced, String finalPodName)
    {
        if (logger.isTraceEnabled())
        {
            linesProduced.forEach(e ->
            {
                logger.trace("Log found [{}]: {}", finalPodName, e);
            });
        }
    }

    @Override
    public LogLineObserver getDefaultLogLineObserver(ExecutionConfig imageExecutionDetails)
    {
        return new LogLineObserverImpl(
            new SimpleLogLineSubscriber().setPodName(imageExecutionDetails.getName())
        );
    }

    public PodPushClient getPodPushClient()
    {
        return podPushClient;
    }

    public Map<String, String> getPodAnnotations()
    {
        if (podAnnotations != null)
        {
            return podAnnotations;
        }
        else
        {
            return podAnnotationClient.getPodAnnotations();
        }
    }
}
