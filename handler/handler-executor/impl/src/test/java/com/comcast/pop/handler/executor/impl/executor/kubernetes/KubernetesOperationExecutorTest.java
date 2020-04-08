package com.comcast.pop.handler.executor.impl.executor.kubernetes;

import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.field.retriever.DefaultLaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.payload.PayloadWriter;
import com.comast.pop.handler.base.payload.PayloadWriterFactory;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.kubernetes.support.reporter.KubernetesReporter;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.client.logging.LogLineObserver;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.fabric8.client.exception.PodException;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollower;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class KubernetesOperationExecutorTest
{
    private JsonHelper jsonHelper = new JsonHelper();

    private KubernetesOperationExecutor executor;
    private Operation operation;
    private KubeConfig kubeConfig;
    private PodConfig podConfig;
    private ExecutionConfig executionConfig;
    private ExecutorContext mockExecutorContext;
    private PayloadWriterFactory mockPayloadWriterFactory;
    private PayloadWriter mockPayloadWriter;
    private LaunchDataWrapper launchDataWrapper;
    private LogLineObserver mockLogLineObserver;
    private PodFollower<PodPushClient> mockPodFollower;

    @BeforeMethod
    public void setup()
    {
        launchDataWrapper = new DefaultLaunchDataWrapper(new String[] {});
        operation = new Operation();
        kubeConfig = new KubeConfig();
        podConfig = new PodConfig();
        executionConfig = new ExecutionConfig();
        mockExecutorContext = mock(ExecutorContext.class);
        mockPayloadWriter = mock(PayloadWriter.class);
        mockPayloadWriterFactory = mock(PayloadWriterFactory.class);
        doReturn(mockPayloadWriter).when(mockPayloadWriterFactory).createWriter(any());
        doReturn(mockPayloadWriterFactory).when(mockExecutorContext).getPayloadWriterFactory();
        doReturn(launchDataWrapper).when(mockExecutorContext).getLaunchDataWrapper();
        mockLogLineObserver = mock(LogLineObserver.class);
        mockPodFollower = mock(PodFollower.class);
        doReturn(mockLogLineObserver).when(mockPodFollower).getDefaultLogLineObserver(any());

        executor = new KubernetesOperationExecutor(mockPodFollower, operation, kubeConfig, podConfig, executionConfig, mockExecutorContext);
    }

    @DataProvider
    public Object[][] testTimeoutExceptionProvider()
    {
        return new Object[][]
            {
                { true },
                { false }
            };
    }

    @Test(dataProvider = "testTimeoutExceptionProvider")
    public void testTimeoutException(final boolean opComplete)
    {
        executor.setIsCompleteOperationProgressRetrievedValue(opComplete);
        executor.setFollower(mockPodFollower);
        PodException podException = new PodException(new TimeoutException());
        doThrow(podException).when(mockPodFollower).startAndFollowPod(any());
        boolean exceptionThrown = false;
        try
        {
            executor.execute(null);
        }
        catch(Exception e)
        {
            exceptionThrown = true;
        }
        if(!exceptionThrown && !opComplete)
            Assert.fail("If the op is NOT complete a timeout exception should have failed as usual.");
        if(exceptionThrown && opComplete)
            Assert.fail("If the op is complete a timeout exception should have no effect.");
    }

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

    @Test
    public void testRetrieveOperationProgressSuccess()
    {
        final ProcessingState EXPECTED_PROCESSING_STATE = ProcessingState.EXECUTING;
        final String EXPECTED_MESSAGE = "Working";
        final Double EXPECTED_PERCENT = 50d;

        configurePodAnnotations(jsonHelper.getJSONString(createOperationProgress(EXPECTED_PROCESSING_STATE, EXPECTED_MESSAGE, EXPECTED_PERCENT)), null);
        OperationProgress operationProgress = executor.retrieveOperationProgress();
        Assert.assertNotNull(operationProgress);
        Assert.assertEquals(operationProgress.getProcessingState(), EXPECTED_PROCESSING_STATE);
        Assert.assertEquals(operationProgress.getProcessingStateMessage(), EXPECTED_MESSAGE);
        Assert.assertEquals(operationProgress.getPercentComplete(), EXPECTED_PERCENT);
    }

    @Test
    public void testRetrieveCompleteOperationProgressSuccess()
    {
        final ProcessingState EXPECTED_PROCESSING_STATE = ProcessingState.COMPLETE;

        configurePodAnnotations(jsonHelper.getJSONString(createOperationProgress(EXPECTED_PROCESSING_STATE, null, null)), null);
        OperationProgress operationProgress = executor.retrieveOperationProgress();
        Assert.assertNotNull(operationProgress);
        Assert.assertEquals(operationProgress.getProcessingState(), EXPECTED_PROCESSING_STATE);
        Assert.assertTrue(executor.getIsCompleteOperationProgressRetrieved().get());
    }

    @Test
    public void testRetrieveOperationProgressPayloadIssue()
    {
        // invalid json
        configurePodAnnotations("{>", null);
        Assert.assertNull(executor.retrieveOperationProgress());
    }

    @Test
    public void testRetrieveOperationProgressError()
    {
        doThrow(new RuntimeException()).when(mockPodFollower).getPodAnnotations();
        Assert.assertNull(executor.retrieveOperationProgress());
    }

    private void configurePodAnnotations(String operationProgressPayload, String resultPayload)
    {
        Map<String, String> annotations = new HashMap<>();
        annotations.put(KubernetesReporter.REPORT_PAYLOAD_ANNOTATION, resultPayload);
        annotations.put(KubernetesReporter.REPORT_PROGRESS_ANNOTATION, operationProgressPayload);
        doReturn(annotations).when(mockPodFollower).getPodAnnotations();
    }

    private OperationProgress createOperationProgress(ProcessingState processingState, String processingStateMessage, Double percentComplete)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        operationProgress.setProcessingStateMessage(processingStateMessage);
        operationProgress.setPercentComplete(percentComplete);
        return operationProgress;
    }

    private CircularFifoQueue<String> generateLogQueue(final int LOG_LINES)
    {
        CircularFifoQueue<String> queue = new CircularFifoQueue<>(KubernetesOperationExecutor.MAX_POD_LOG_LINES);
        IntStream.range(0, LOG_LINES).forEach(i -> queue.add(Integer.toString(i)));
        return queue;
    }
}
