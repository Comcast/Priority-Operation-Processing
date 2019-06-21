package com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class KubernetesOperationExecutorTest
{
    @DataProvider
    public Object[][] generateFailedOperationProgressProvider()
    {
        return new Object[][]
            {
                // nothing
                {0, 0, null},
                // 1 (with exception)
                {1, 1, new RuntimeException()},
                // under
                {5, Math.min(5, KubernetesOperationExecutor.DEFAULT_FAIL_DIAGNOSTIC_LINE_LIMIT), null},
                // even
                {KubernetesOperationExecutor.DEFAULT_FAIL_DIAGNOSTIC_LINE_LIMIT, KubernetesOperationExecutor.DEFAULT_FAIL_DIAGNOSTIC_LINE_LIMIT, null},
                // over
                {4096, KubernetesOperationExecutor.DEFAULT_FAIL_DIAGNOSTIC_LINE_LIMIT, null}
            };
    }

    @Test(dataProvider = "generateFailedOperationProgressProvider")
    public void testGenerateFailedOperationProgress(final int LOG_LINES, final int EXPECTED_LOG_LINES, Exception exception)
    {
        final String OPERATION_NAME = UUID.randomUUID().toString();
        final String DIAGNOSTIC_MESSAGE = UUID.randomUUID().toString();

        OperationProgress operationProgress = KubernetesOperationExecutor.generateFailedOperationProgress(
            OPERATION_NAME,
            DIAGNOSTIC_MESSAGE,
            exception,
            generateLogQueue(LOG_LINES)
        );

        Assert.assertNotNull(operationProgress);
        Assert.assertEquals(operationProgress.getOperation(), OPERATION_NAME);
        Assert.assertNotNull(operationProgress.getDiagnosticEvents());
        Assert.assertEquals(operationProgress.getDiagnosticEvents().length, 1);
        DiagnosticEvent diagnosticEvent = operationProgress.getDiagnosticEvents()[0];
        Assert.assertEquals(diagnosticEvent.getMessage(), DIAGNOSTIC_MESSAGE);
        if(exception != null)
            Assert.assertNotNull(diagnosticEvent.getStackTrace());
        else
            Assert.assertNull(diagnosticEvent.getStackTrace());
        Assert.assertNotNull(diagnosticEvent.getPayload());
        Assert.assertTrue(diagnosticEvent.getPayload() instanceof List);
        List<String> diagnosticLines = (List<String>)diagnosticEvent.getPayload();
        Assert.assertEquals(diagnosticLines.size(), EXPECTED_LOG_LINES);
        int diagnosticLineIndex = 0;
        for(int numberEntry = LOG_LINES - EXPECTED_LOG_LINES; numberEntry < LOG_LINES; numberEntry++)
        {
            Assert.assertEquals(diagnosticLines.get(diagnosticLineIndex), Integer.toString(numberEntry));
            diagnosticLineIndex++;
        }
    }

    private CircularFifoQueue<String> generateLogQueue(final int LOG_LINES)
    {
        CircularFifoQueue<String> queue = new CircularFifoQueue<>(KubernetesOperationExecutor.MAX_POD_LOG_LINES);
        IntStream.range(0, LOG_LINES).forEach(i -> queue.add(Integer.toString(i)));
        return queue;
    }
}
