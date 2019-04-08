package com.theplatform.dfh.cp.handler.base.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilities class for basic thread functionality (logging primarily)
 */
public class ThreadUtils
{
    private static Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    private ThreadUtils(){}

    /**
     * Logs all the threads that are alive and flattened callstacks
     */
    public static void logAliveThreads()
    {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Alive Threads: ");
        List<Thread> aliveThreads = threadSet.stream()
            .filter(Thread::isAlive)
            .collect(Collectors.toList());
        stringBuilder.append(aliveThreads.stream()
            .map(thread -> thread.getName()
                + "[" + thread.getState().name() + "]"
                + "::"
                + getStackTraceString(thread))
            .collect(Collectors.joining(" --- "))
        );
        logger.info(stringBuilder.toString());
    }

    /**
     * Logs all the threads that are alive and flattened callstacks (after a specified delay)
     * @param delay How long to delay before logging the threads
     */
    public static void logAliveThreads(Long delay)
    {
        try
        {
            Thread.sleep(delay);
            logAliveThreads();
        }
        catch(InterruptedException e)
        {
            logger.warn("logAliveThreads interuppted", e);
        }
    }

    public static String getStackTraceString(Thread t)
    {
        return Arrays.toString(t.getStackTrace());
    }
}
