package com.theplatform.dfh.cp.modules.kube.client;

import java.util.List;

/**
 * Collect the execution logs on the pod
 */
public interface LogLineAccumulator
{
    public void appendLine(String s);

    public List<String> takeAll();

    boolean isAllLogDataRequired();

    void setCompletion(Runnable runnable);

    void setCompletionIdentifier(String endOfLogIdentifier);

    void forceCompletion();
}
