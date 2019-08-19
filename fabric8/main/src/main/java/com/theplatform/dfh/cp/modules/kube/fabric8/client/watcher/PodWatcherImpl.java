package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.exception.PodException;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.PodResourceFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.K8LogReader;
import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private PodResourceFacade podResource;
    private K8LogReader k8LogReader;
    private LogLineAccumulator logLineAccumulator;
    private FinalPodPhaseInfo finalPodPhaseInfo;
    private Watch watch;
    private int resetCounter = 0;
    private ConnectionTracker connectionTracker;
    private List<PodEventListener> eventListeners = new ArrayList<>();

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

    @Override
    public void addEventListeners(List<PodEventListener> listeners)
    {
        this.eventListeners = listeners;
    }

    public String getPodName()
    {
        return podName;
    }

    public void setPodName(String podName)
    {
        this.podName = podName;
    }

    public void setConnectionTracker(ConnectionTracker connectionTracker)
    {
        this.connectionTracker = connectionTracker;
    }

    @Override
    public void eventReceived(Action action, Pod pod)
    {
        fireEventReceived(action, pod);
        PodStatus podStatus = pod.getStatus();
        pod.setSpec(null);

        PodPhase podPhase = PodPhase.fromPodStatus(pod.getStatus());

        logger.debug("The phase of the pod {} is currently: {} for action {}", podName, podPhase, action);

        logger.trace("Pod {} {} {}", action, podStatus, pod);

        if (action == Action.DELETED)
        {
            logger.error("Pod {} was deleted", podName);
            finalPodPhaseInfo = new FinalPodPhaseInfo(podName, PodPhase.UNKNOWN, 0, "PodDeleted");
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
            logger.debug("Calling intializeAndStartLogObservation for pod: {}", podName);
            scheduledLatch.countDown();
            intializeAndStartLogObservation();
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
                    logger.debug("Calling intializeAndStartLogObservation for COMPLETED pod: {}", podName);
                    intializeAndStartLogObservation();
                }
            }
            else
            {
                gracefulShutdown();
            }
        }
    }

    private void fireEventReceived(Action action, Pod pod)
    {
        for (PodEventListener eventListener : eventListeners)
        {
            eventListener.eventReceived(action, pod);
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

    private synchronized void intializeAndStartLogObservation()
    {
        if(k8LogReader == null)
            k8LogReader = new K8LogReader(podName, logLineAccumulator, connectionTracker);

        Pod pod = podResource.get();
        if (pod == null)
        {
            throw new PodException("The pod " + podName + " can't be followed for logging.");
        }
        logger.debug("Pod {} has phase {}", podName, pod.getStatus().getPhase());
        LogWatch logWatch = podResource.watchLog();
        k8LogReader.observeRuntimeLog(logWatch);

    }

    private void extractLogsForFastFail()
    {
        String log = podResource.getLog();
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
        logger.warn("[{}]Log watch is being reset", podName);
        if(k8LogReader != null)
        {
            k8LogReader.shutdown();
        }
        else
        {
            // may be that the pod complete so quickly this did not get initialized
            logger.warn("[{}]resetLogging called without a k8LogReader configured.", podName);
        }

        this.resetCounter++;
        if(resetCounter > MAX_LOGGING_RESETS_BEFORE_WE_CHECK_FOR_INFINITE_LOOP)
        {
            if(logLineAccumulator.isAllLogDataRequired() && finalPodPhaseInfo != null)
            {
                logger.warn("[{}]Waited too long. Truncating our wait for log data.", podName);
                logLineAccumulator.forceCompletion();
            }
        } else
        {
            // allow the log reader to be re-created
            k8LogReader = null;
            intializeAndStartLogObservation();
        }
    }

    public void setPodResource(PodResourceFacade podResource)
    {
        this.podResource = podResource;
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
