package com.comcast.pop.modules.kube.client.logging;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Used to send logs to consumers
 */
public interface LogLineObserver
{
    public LogLineObserver setPodName(String podName);

    public void addConsumer(Consumer<String> consumer);

    public void addAggregateConsumer(Consumer<Collection<String>> consumer);

    public void send(String s);

    public void send(Collection<String> lines);

    public void done();
}