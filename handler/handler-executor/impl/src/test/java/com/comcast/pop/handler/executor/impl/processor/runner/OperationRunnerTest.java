package com.comcast.pop.handler.executor.impl.processor.runner;

import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.executor.BaseOperationExecutor;
import com.comcast.pop.handler.executor.impl.executor.OperationExecutorFactory;
import com.comcast.pop.handler.executor.impl.processor.OnOperationCompleteListener;
import com.comcast.pop.handler.executor.impl.processor.OperationWrapper;
import com.comcast.pop.handler.executor.impl.progress.agenda.AgendaProgressReporter;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OperationRunnerTest
{
    private OperationRunner operationRunner;
    private OperationWrapper operationWrapper;
    private ExecutorContext mockExecutorContext;
    private OnOperationCompleteListener mockOnOperationCompleteListener;
    private OperationExecutorFactory mockOperationExecutorFactory;
    private AgendaProgressReporter mockAgendaProgressReporter;
    private BaseOperationExecutor mockOperationExecutor;

    @BeforeMethod
    public void setup()
    {
        operationWrapper = new OperationWrapper(new Operation());
        mockExecutorContext = mock(ExecutorContext.class);
        mockOnOperationCompleteListener = mock(OnOperationCompleteListener.class);
        mockOperationExecutor = mock(BaseOperationExecutor.class);
        mockOperationExecutorFactory = mock(OperationExecutorFactory.class);
        mockAgendaProgressReporter = mock(AgendaProgressReporter.class);
        doReturn(mockAgendaProgressReporter).when(mockExecutorContext).getAgendaProgressReporter();
        doReturn(mockOperationExecutorFactory).when(mockExecutorContext).getOperationExecutorFactory();
        doReturn(mockOperationExecutor).when(mockOperationExecutorFactory).generateOperationExecutor(any(), any());
        operationRunner = new OperationRunner(operationWrapper, mockExecutorContext, mockOnOperationCompleteListener);
    }


    @DataProvider
    public Object[][] operationProgressProvider()
    {
        return new Object[][]
            {
                {null, false},
                {new OperationProgress(), false},
                {createOperationProgress(ProcessingState.EXECUTING, false), false},
                {createOperationProgress(ProcessingState.WAITING, false), false},
                {createOperationProgress(ProcessingState.COMPLETE, true), true},
            };
    }

    @Test(dataProvider = "operationProgressProvider")
    public void testRunOperationProgressResult(OperationProgress operationProgress, final Boolean expectedSuccess)
    {
        doReturn(operationProgress).when(mockOperationExecutor).retrieveOperationProgress();
        operationRunner.run();
        verify(mockOperationExecutor, times(1)).execute(anyString());
        if (expectedSuccess)
        {
            Assert.assertNull(operationWrapper.getDiagnosticEvents());
        }
        else
        {
            Assert.assertNotNull(operationWrapper.getDiagnosticEvents());
            Assert.assertEquals(operationWrapper.getDiagnosticEvents().size(), 1);
        }
        Assert.assertEquals(operationWrapper.getSuccess(), expectedSuccess);
    }

    private OperationProgress createOperationProgress(ProcessingState processingState, boolean succeeded)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        if(succeeded)
            operationProgress.setProcessingStateMessage(CompleteStateMessage.SUCCEEDED.toString());
        return operationProgress;
    }
}
