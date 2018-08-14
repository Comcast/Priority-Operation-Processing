package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class LogLineAccumulatorTest
{
    public static final String COMPLETION_TERMINATION = "foo";

    @Test
    public void testName() throws Exception
    {
        LogLineAccumulator logLineAccumulator = new LogLineAccumulatorImpl();
        Assert.assertFalse(logLineAccumulator.isAllLogDataRequired());

        logLineAccumulator.setCompletionIdentifier(COMPLETION_TERMINATION);
        Assert.assertTrue(logLineAccumulator.isAllLogDataRequired());
    }

    @Test
    public void testCompletion() throws Exception
    {
        LogLineAccumulator logLineAccumulator = new LogLineAccumulatorImpl();
        logLineAccumulator.setCompletionIdentifier(COMPLETION_TERMINATION);

        AtomicBoolean runnableRan = new AtomicBoolean(false);

        logLineAccumulator.setCompletion(new Runnable()
        {
            @Override
            public void run()
            {
                runnableRan.set(true);
            }
        });

        String SOME_VALUE_NOT_EQUAL_TO_COMPLETION = "boo";
        logLineAccumulator.appendLine(SOME_VALUE_NOT_EQUAL_TO_COMPLETION);
        Assert.assertFalse(runnableRan.get());
        logLineAccumulator.appendLine(COMPLETION_TERMINATION);
        logLineAccumulator.takeAll();
        Assert.assertTrue(runnableRan.get());
    }

    @Test
    public void testExceptionInCompletion() throws Exception
    {
        LogLineAccumulator logLineAccumulator = new LogLineAccumulatorImpl();
        logLineAccumulator.setCompletionIdentifier(COMPLETION_TERMINATION);

        AtomicBoolean runnableRan = new AtomicBoolean(false);
        logLineAccumulator.setCompletion(new Runnable()
        {
            @Override
            public void run()
            {
                runnableRan.set(true);
                throw new RuntimeException();
            }
        });

        logLineAccumulator.appendLine(COMPLETION_TERMINATION);
        logLineAccumulator.takeAll();
        Assert.assertTrue(runnableRan.get());
    }
}
