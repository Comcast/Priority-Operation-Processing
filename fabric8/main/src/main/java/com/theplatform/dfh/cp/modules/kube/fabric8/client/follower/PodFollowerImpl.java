package com.theplatform.dfh.cp.modules.kube.fabric8.client.follower;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClientImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.exception.PodException;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.exception.PodNotScheduledException;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineObserverImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.SimpleLogLineSubscriber;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcher;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper.getFabric8Config;

/**
 * A PodFollower is capable of following multiple pods, one per invocation of "startAndFollowPod(...)"
 * The only state kept at object-instance level is immutable and used for all pod executions.
 */
public class PodFollowerImpl<C extends PodPushClient> implements PodFollower<C>
{
    public static final int LATCH_TIMEOUT = 1000;
    public static final int MAX_INACTIVITY_BEFORE_LOG_RESET = 20;
    private static Logger logger = LoggerFactory.getLogger(PodFollowerImpl.class);

    private PodPushClientFactoryImpl podPushClientFactory = new PodPushClientFactoryImpl();
    private PodPushClient podPushClient;

    public PodFollowerImpl(KubeConfig kubeConfig)
    {
        podPushClient = podPushClientFactory.getClient(kubeConfig);
    }

    @Override
    public FinalPodPhaseInfo startAndFollowPod(PodConfig podConfig, ExecutionConfig executionConfig,
        LogLineObserver logLineObserver)
    {
        String podName = executionConfig.getName();
        PodWatcher podWatcher = null;
        FinalPodPhaseInfo finalPhase = null;
        try
        {
            CountDownLatch podScheduled = new CountDownLatch(1);
            CountDownLatch podFinishedSuccessOrFailure = new CountDownLatch(1);

            // START POD
            podWatcher = podPushClient
                .start(podConfig, executionConfig, podScheduled, podFinishedSuccessOrFailure);

            // CHECK POD: SCHEDULED
            // Doesn't need the podWatcher, since we have a latch to wait on.
            schedulePodCheck(podConfig, podName, podScheduled);

            // CHECK POD: SUCCESS/FAILURE
            keepCheckForFinishedState(podConfig, executionConfig, logLineObserver, podFinishedSuccessOrFailure, podWatcher);
        }
        catch (Exception e)
        {
            logger.error("Exception occured for podName {}", podName, e);
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
            resetableScheduleTimeout.timeout();
        }
        while (!isScheduled);
        logger.debug("Pod {} passed scheduling watch.", podName);
    }

    private void keepCheckForFinishedState(PodConfig podConfig, ExecutionConfig executionConfig, LogLineObserver logLineObserver,
        CountDownLatch podFinishedSuccessOrFailure, PodWatcher podWatcher)
        throws InterruptedException, TimeoutException
    {
        boolean isFinished = false;
        ResetableTimeout resetableProductivityTimeout = new ResetableTimeout(
            podConfig.getPodStdoutTimeout());
        do
        {
            isFinished = runTillFinished(
                logLineObserver, executionConfig.getName(), podFinishedSuccessOrFailure, executionConfig.getLogLineAccumulator(),
                resetableProductivityTimeout, podWatcher);
        }
        while (!isFinished);
    }

    private boolean runTillFinished(LogLineObserver logLineObserver, String podName,
        CountDownLatch podFinishedSuccessOrFailure, LogLineAccumulator logLineAccumulator,
        ResetableTimeout resetableProductivityTimeout, PodWatcher podWatcher) throws InterruptedException, TimeoutException
    {
        boolean isFinished;
        isFinished = podFinishedSuccessOrFailure.await(LATCH_TIMEOUT, TimeUnit.MILLISECONDS);
        logger.trace("Waiting for end state of pod {}", podName);
        List<String> linesProduced = logLineAccumulator.takeAll();
        int size = linesProduced.size();
        if (size > 0)
        {
            resetableProductivityTimeout.reset();
            logger.debug("Lines produced for [{}] : {}", podName, size);
            logLineObserver.send(linesProduced);
        }

        logThisPodsLogs(linesProduced, podName);
        resetableProductivityTimeout.timeout();
        int inactivityCounter = resetableProductivityTimeout.getInactivityCounter();
        logger.info("Current pod {} log inactivity counter {}, max {}", podName, inactivityCounter, MAX_INACTIVITY_BEFORE_LOG_RESET);
        if(inactivityCounter > MAX_INACTIVITY_BEFORE_LOG_RESET)
        {
            logger.warn("Noticing inactivity on logging system. ");
            podWatcher.resetLogging();
        }
        return isFinished;
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
        LogLineObserver logLineObserver = new LogLineObserverImpl(
            new SimpleLogLineSubscriber().setPodName(imageExecutionDetails.getName())
        );
        return logLineObserver;
    }

    public PodPushClient getPodPushClient()
    {
        return podPushClient;
    }
}
