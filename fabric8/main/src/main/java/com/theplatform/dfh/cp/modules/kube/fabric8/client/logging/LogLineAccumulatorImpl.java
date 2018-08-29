package com.theplatform.dfh.cp.modules.kube.fabric8.client.logging;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LogLineAccumulatorImpl implements LogLineAccumulator
{
    private static Logger logger = LoggerFactory.getLogger(LogLineAccumulatorImpl.class);
    private final List<String> logs = Collections.synchronizedList(new LinkedList<String>());
    private Runnable onCompletion;
    private String completionIdentifier;
    private boolean loggingComplete = false;

    public void appendLine(String s)
    {
        logger.trace("Log line being added. {}", s);
        synchronized (logs)
        {
            logs.add(s);
        }
    }

    private void finish()
    {
        try
        {
            onCompletion.run();
        }
        catch (Throwable t)
        {
            logger.error("Caught exception ", t);
        }
    }

    private void updateLoggingComplete(String s)
    {
        if( s != null
            && completionIdentifier != null
            && s.contains(completionIdentifier))
        {
            loggingComplete = true;
        }
    }

    public List<String> takeAll()
    {
        LinkedList<String> allLines = new LinkedList<>();
        synchronized (logs)
        {
            logs.forEach(s ->
                {
                    allLines.add(s);
                    updateLoggingComplete(s);
                }
            );
            if(loggingComplete && onCompletion != null)
            {
                finish();
            }
            logs.clear();
        }
        return allLines;
    }

    @Override
    public boolean isAllLogDataRequired()
    {
        return completionIdentifier != null;
    }

    @Override
    public void setCompletionIdentifier(String endOfLogIdentifier)
    {
        this.completionIdentifier = endOfLogIdentifier;
    }

    @Override
    public void setCompletion(Runnable runnable)
    {
        this.onCompletion = runnable;
    }

    @Override
    public void forceCompletion()
    {
        if(onCompletion != null)
        {
            finish();
        }
    }
}
