package com.comcast.pop.modules.kube.client;

import java.util.List;

/**
 * Collect the execution logs on the pod
 */
public interface LogLineAccumulator
{
    void appendLine(String s);

    List<String> takeAll();

    boolean isAllLogDataRequired();

    void setCompletion(Runnable runnable);

    void setCompletionIdentifier(String endOfLogIdentifier);

    void forceCompletion();
}
