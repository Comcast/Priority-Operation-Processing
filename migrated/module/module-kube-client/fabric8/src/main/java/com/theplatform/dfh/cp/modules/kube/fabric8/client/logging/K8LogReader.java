package com.theplatform.dfh.cp.modules.kube.fabric8.client.logging;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.ConnectionTracker;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;

import java.io.InputStreamReader;

/**
 * Wrap the JavaRx work
 */
public class K8LogReader
{
    private static Logger logger = LoggerFactory.getLogger(K8LogReader.class);

    // when closing the log reader down the associated sockets need to have their timeout reset
    private final int SHUTDOWN_SOCKET_TIMEOUT = 5000;

    private String podName;
    private Scheduler subscriptionScheduler;
    private Scheduler observerScheduler;
    private Subscription subscription;
    private Observable<String> stringObservable;
    private LogLineAccumulator logLineAccumulator;
    private LogWatch logWatch;
    private ConnectionTracker connectionTracker;

    public K8LogReader(String podName, LogLineAccumulator logLineAccumulator, ConnectionTracker connectionTracker)
    {
        this.podName = podName;
        this.logLineAccumulator = logLineAccumulator;
        this.connectionTracker = connectionTracker;
    }

    public Observable<String> observeRuntimeLog(LogWatch logWatch)
    {
        logger.debug("Starting observable for {}", podName);
        if(subscriptionScheduler == null && observerScheduler == null)
        {
            subscriptionScheduler = Schedulers.newThread();
            observerScheduler = Schedulers.newThread();
        }

        this.logWatch = logWatch;

        Action1<Throwable> onError = new Action1<Throwable>()
        {
            @Override
            public void call(Throwable throwable)
            {
                logger.warn("IO issue in stream [{}] as {}", throwable.getMessage(), throwable.getClass());
                subscription.unsubscribe();
            }
        };
        stringObservable =
            StringObservable.byLine(
                StringObservable.from(
                    new InputStreamReader(logWatch.getOutput())).doOnError(onError)
            ).observeOn(observerScheduler, false);

        subscription = stringObservable.subscribeOn(subscriptionScheduler)
            .subscribe(logLineAccumulator::appendLine);

        return stringObservable;
    }

    public void shutdown()
    {
        if (logWatch != null)
        {
            try
            {
                logger.debug("k8 logWatch is closing for pod: {}", podName);
                // everything is shutting down for this pod execution, reset all the socket timeouts
                // this is necessary because the socket for logging is opened with no timeout (0) and when shutting down the act of flushing may result in another socket read
                if(connectionTracker != null)
                    connectionTracker.setSocketTimeouts(SHUTDOWN_SOCKET_TIMEOUT);

                logWatch.close();
                logWatch = null;
            }
            catch (Exception e)
            {
                logger.error("Exception: ", e);
            }
        }
        logger.debug("Log reader is being shutdown for pod: {}", podName);
        if (subscription != null)
        {
            subscription.unsubscribe();
        }
    }

    public String getPodName()
    {
        return podName;
    }

    public void setPodName(String podName)
    {
        this.podName = podName;
    }
}