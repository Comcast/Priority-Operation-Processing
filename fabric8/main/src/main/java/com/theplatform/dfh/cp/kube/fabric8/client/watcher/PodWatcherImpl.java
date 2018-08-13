package com.theplatform.dfh.cp.kube.fabric8.client.watcher;

import com.theplatform.dfh.cp.kube.fabric8.client.exception.PodException;
import com.theplatform.dfh.cp.kube.fabric8.client.logging.K8LogReader;
import com.theplatform.dfh.cp.kube.client.LogLineAccumulator;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * This is an implementor of the fabric8 interface "Watcher".
 * PodWatcher holds onto a fabric8 "Watch" class and responds to events.
 */
public class PodWatcherImpl implements Watcher<Pod>, PodWatcher
{
    private static Logger logger = LoggerFactory.getLogger(PodWatcherImpl.class);

    public static final int MAX_LOGGING_RESETS_BEFORE_WE_CHECK_FOR_INFINITE_LOOP = 2;

    private CountDownLatch finishedLatch;
    private CountDownLatch scheduledLatch;
    private String podName;
    private PodResource<Pod, DoneablePod> podClient;
    private K8LogReader k8LogReader;
    private LogLineAccumulator logLineAccumulator;
    private FinalPodPhaseInfo finalPodPhaseInfo;
    private Watch watch;
    private int resetCounter = 0;

    public void setFinishedLatch(CountDownLatch finishedLatch)
    {
        this.finishedLatch = finishedLatch;
    }

    public void setScheduledLatch(CountDownLatch scheduledLatch)
    {
        this.scheduledLatch = scheduledLatch;
    }

    public LogLineAccumulator getLogLineAccumulator()
    {
        return logLineAccumulator;
    }

    public void setLogLineAccumulator(
        LogLineAccumulator logLineAccumulator)
    {
        this.logLineAccumulator = logLineAccumulator;
    }

    public String getPodName()
    {
        return podName;
    }

    public void setPodName(String podName)
    {
        this.podName = podName;
    }

    @Override
    public void eventReceived(Action action, Pod pod)
    {

        PodStatus podStatus = pod.getStatus();
        pod.setSpec(null);

        PodPhase podPhase = PodPhase.fromPodStatus(pod.getStatus());

        logger.debug("The phase of the pod {} is currently: {} for action {}", podName, podPhase, action);

        logger.trace("Pod {} {} {}", action, podStatus, pod);

        if (action == Action.DELETED)
        {
            logger.error("Pod {} was deleted", podName);
            finalPodPhaseInfo = new FinalPodPhaseInfo(podName, PodPhase.UNKNOWN, 0);
            scheduledLatch.countDown();
            try
            {
                gracefulShutdown();
            }
            catch (Throwable t)
            {
                logger.error("Failure to shutdown", t);
            }

            return;
        }

        if (podPhase.equals(PodPhase.RUNNING) && k8LogReader == null)
        {
            logger.debug("Starting new logwatcher.");
            scheduledLatch.countDown();
            intializeAndStartLogObeservation();
        }

        if (podPhase.hasFinished())
        {
            finalPodPhaseInfo = FinalPodPhaseInfo.fromPodStatus(podName, pod.getStatus());
            if (podPhase.isFailed())
            {
                logger.error("Pod {} failed exitCode: {}", podName, finalPodPhaseInfo.exitCode);
            }
            else
            {
                logger.info("Pod {} completed without error.", podName);
            }
            scheduledLatch.countDown();// just incase we transition past running.
            if (logLineAccumulator.isAllLogDataRequired() && !podPhase.isFailed())
            {
                logger.debug("latch close is being delegated to logLineAccumulator {}", podName);
                logLineAccumulator.setCompletion(
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            logger.debug("Closing last latch for {}.", podName);
                            watch.close();
                            finishedLatch.countDown();
                        }
                    });
                // we failed or succeeded so quickly we didn't get logs.
                if (k8LogReader == null)
                {
                    intializeAndStartLogObeservation();
                }
            }
            else
            {
                gracefulShutdown();
            }
        }
    }

    private void gracefulShutdown()
    {
        if (k8LogReader == null)
        {
            extractLogsForFastFail();
        }
        watch.close();
        finishedLatch.countDown();
    }

    private void intializeAndStartLogObeservation()
    {
        k8LogReader = new K8LogReader(podName, logLineAccumulator);
        setupLogObserveration();
    }

    private void setupLogObserveration()
    {
        Pod pod = podClient.get();
        if (pod == null)
        {
            throw new PodException("The pod " + podName + " can't be followed for logging.");
        }
        logger.debug("Pod {} has phase {}", podName, pod.getStatus().getPhase());
        LogWatch logWatch = podClient.watchLog();
        k8LogReader.observeRuntimeLog(logWatch);
    }

    private void extractLogsForFastFail()
    {
        String log = podClient.getLog();
        if (log != null)
        {
            Arrays.stream(log.split("\n")).forEach(logLineAccumulator::appendLine);
        }
    }

    public FinalPodPhaseInfo getFinalPodPhaseInfo()
    {
        return finalPodPhaseInfo;
    }

    @Override
    public void onClose(KubernetesClientException cause)
    {
        if (watch != null)
        {
            watch.close();
        }

        if (k8LogReader != null)
        {
            k8LogReader.shutdown();
        }
    }

    @Override
    public void resetLogging()
    {
        logger.warn("Log watch is being reset");
        k8LogReader.shutdown();
        this.resetCounter++;
        if(resetCounter > MAX_LOGGING_RESETS_BEFORE_WE_CHECK_FOR_INFINITE_LOOP)
        {
            if(logLineAccumulator.isAllLogDataRequired() && finalPodPhaseInfo != null)
            {
                logger.warn("Waited to long. Truncating our wait for log data.");
                logLineAccumulator.forceCompletion();
            }
        }
        setupLogObserveration();
    }

    public void setPodClient(PodResource<Pod, DoneablePod> podClient)
    {
        this.podClient = podClient;
    }

    public void setWatch(Watch watch)
    {
        this.watch = watch;
    }

    public Watch getWatch()
    {
        return watch;
    }
}
