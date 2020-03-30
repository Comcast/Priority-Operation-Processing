package com.comcast.fission.handler.executor.impl.executor.resident;

import com.comcast.fission.handler.executor.impl.messages.ExecutorMessages;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

public class ResidentOperationExecutorTest
{
    private final String OP_NAME = "theOp";
    private ResidentOperationExecutor executor;
    private Operation operation;
    private ResidentHandler mockResidentHandler;
    private LaunchDataWrapper mockLaunchDataWrapper;

    @BeforeMethod
    public void setup()
    {
        operation = new Operation();
        operation.setName(OP_NAME);

        mockResidentHandler = mock(ResidentHandler.class);
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        executor = new ResidentOperationExecutor(operation, mockResidentHandler, mockLaunchDataWrapper);
    }

    @Test
    public void testGenerateOperationProgressNoIssuesDefaultProgress()
    {
        OperationProgress progress = executor.generateOperationProgress(null);
        validateFields(progress, ProcessingState.WAITING, null);
    }

    @Test
    public void testGenerateOperationProgressNoIssues()
    {
        ProcessingState EXPECTED_STATE = ProcessingState.EXECUTING;

        OperationProgress currentProgress = createOperationProgress(EXPECTED_STATE, null);
        OperationProgress progress = executor.generateOperationProgress(currentProgress);
        validateFields(progress, EXPECTED_STATE, null);
    }

    @Test
    public void testGenerateOperationProgressWithException()
    {
        ProcessingState EXPECTED_STATE = ProcessingState.COMPLETE;

        executor.setResidentHandlerException(new RuntimeException());
        OperationProgress currentProgress = createOperationProgress(EXPECTED_STATE, null);
        OperationProgress progress = executor.generateOperationProgress(currentProgress);
        validateFields(progress, EXPECTED_STATE, new DiagnosticEvent[]
            {
                new DiagnosticEvent(ExecutorMessages.OPERATION_RESIDENT_EXECUTION_FAILED.getMessage(operation.getName()))
            });
    }

    @Test
    public void testGenerateOperationProgressWithExceptionAndDiagnostics()
    {
        final String DIAG_MESSAGE = "opDiagnosticMessage";

        ProcessingState EXPECTED_STATE = ProcessingState.COMPLETE;
        executor.setResidentHandlerException(new RuntimeException());
        OperationProgress currentProgress = createOperationProgress(EXPECTED_STATE, new DiagnosticEvent[]
            {
                new DiagnosticEvent(DIAG_MESSAGE)
            });
        OperationProgress progress = executor.generateOperationProgress(currentProgress);
        validateFields(progress, EXPECTED_STATE, new DiagnosticEvent[]
            {
                new DiagnosticEvent(ExecutorMessages.OPERATION_RESIDENT_EXECUTION_FAILED.getMessage(operation.getName())),
                new DiagnosticEvent(DIAG_MESSAGE)
            });
    }

    private OperationProgress createOperationProgress(ProcessingState state, DiagnosticEvent[] diagnosticEvents)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(state);
        operationProgress.setDiagnosticEvents(diagnosticEvents);
        return operationProgress;
    }

    private void validateFields(OperationProgress operationProgress, ProcessingState expectedState, DiagnosticEvent[] diagnosticEvents)
    {
        Assert.assertEquals(operationProgress.getProcessingState(), expectedState);
        Assert.assertEquals(operationProgress.getOperation(), OP_NAME);
        if(diagnosticEvents == null)
            Assert.assertNull(operationProgress.getDiagnosticEvents());
        else
        {
            Assert.assertNotNull(operationProgress.getDiagnosticEvents());
            Assert.assertEquals(operationProgress.getDiagnosticEvents().length, diagnosticEvents.length);
            for(DiagnosticEvent de : diagnosticEvents)
            {
                Assert.assertTrue(Arrays.stream(operationProgress.getDiagnosticEvents())
                    .anyMatch(actualEvent -> StringUtils.equals(de.getMessage(), actualEvent.getMessage()))
                , String.format("Missing event with message: %1$s", de.getMessage()));
            }
        }
    }
}
