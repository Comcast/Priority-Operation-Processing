package com.comcast.pop.modules.kube.fabric8.client.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleLogLineSubscriber extends Subscriber<String>
{
    private static Logger logger = LoggerFactory.getLogger(SimpleLogLineSubscriber.class);
    protected String podName;
    protected AtomicInteger linesSeen = new AtomicInteger(0);


    public Integer getLinesSeen()
    {
        return linesSeen.get();
    }

    public String getPodName()
    {
        return podName;
    }

    public SimpleLogLineSubscriber setPodName(String podName)
    {
        this.podName = podName;
        return this;
    }

    @Override
    public void onCompleted()
    {
        logger.debug("Subscriber [{}] done! w/total line count {}", podName, linesSeen);
    }

    @Override
    public void onError(Throwable e)
    {
        logger.error("Exception for [{}]", podName, e);
    }

    @Override
    public void onNext(String s)
    {
        int lines = linesSeen.incrementAndGet();
        if(lines % 100 == 0)
        {
            logger.debug("Processed {} lines for [{}]", lines, podName);
        }
        logger.trace("Processing line for [{}]: {}", podName, s);
    }
}