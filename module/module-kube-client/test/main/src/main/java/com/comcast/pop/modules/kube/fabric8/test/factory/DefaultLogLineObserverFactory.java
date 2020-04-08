package com.comcast.pop.modules.kube.fabric8.test.factory;

import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.logging.LogLineObserver;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class DefaultLogLineObserverFactory
{
    private static Logger logger = LoggerFactory.getLogger(DefaultLogLineObserverFactory.class);


    public static LogLineObserver getChattyLogLineObserver(ExecutionConfig executionConfig,
        PodFollower follower, int nthLineShouldLog)
    {
        AtomicInteger counter = new AtomicInteger(0);
        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);
        logLineObserver.addConsumer(s ->
        {
            int cur = counter.incrementAndGet();
            if(cur==nthLineShouldLog)
            {
                logger.info(s);
                counter.set(0);
            }
        });
        return logLineObserver;
    }

    public static LogLineObserver getLogLineObserver(ExecutionConfig executionConfig, PodFollower follower)
    {
        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);
        logLineObserver.addConsumer(s ->
        {
            if (s.length() > 70)
                logger.trace("More then 60 characters found!");
        });
        return logLineObserver;
    }
}