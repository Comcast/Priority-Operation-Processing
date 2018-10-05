package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.OperationRunnerFactory;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class OperationConductorTest
{
    private OperationConductor operationConductor;
    private OperationRunnerFactory mockOperationRunnerFactory;
    private ExecutorContext mockExecutorContext;
    private JsonContext mockJsonContext;
    private ExecutorService mockExecutorService;

    @BeforeMethod
    public void setup()
    {
        mockExecutorContext = mock(ExecutorContext.class);
        mockJsonContext = mock(JsonContext.class);
        doReturn(mockJsonContext).when(mockExecutorContext).getJsonContext();
        mockExecutorService = mock(ExecutorService.class);
        mockOperationRunnerFactory = mock(OperationRunnerFactory.class);
        operationConductor = new OperationConductor(new ArrayList<>(), mockExecutorContext);
        operationConductor.setOperationRunnerFactory(mockOperationRunnerFactory);
        operationConductor.setExecutorService(mockExecutorService);
    }

    @Test
    public void testGetReadyOperations()
    {
        Collection<String> readyOps = addOperations(2, true, operationConductor.getPendingOperations());
        // add some non-ready
        addOperations(3, false, operationConductor.getPendingOperations());
        Collection<OperationWrapper> actualReadyOperations = operationConductor.getReadyOperations();
        Assert.assertEquals(actualReadyOperations.size(), readyOps.size());
        readyOps.forEach(readyOp ->
            Assert.assertTrue(actualReadyOperations.stream().anyMatch(actualOp -> readyOp.equals(actualOp.getOperation().getName())))
        );
    }

    @Test(expectedExceptions = AgendaExecutorException.class, expectedExceptionsMessageRegExp = ".*deadlock.*")
    public void testDeadlock()
    {
        // no pending ops, no running ops
        operationConductor.launchReadyPendingOperations();
    }

    @Test
    public void testReadyOperationsLaunched()
    {
        Collection<String> operationsToLaunch = addOperations(3, true, operationConductor.getPendingOperations());
        operationConductor.launchReadyPendingOperations();
        Assert.assertEquals(operationConductor.getPendingOperations().size(), 0);
        Assert.assertEquals(operationConductor.getRunningOperations().size(), operationsToLaunch.size());
        verify(mockExecutorService, times(operationsToLaunch.size())).submit(any(Runnable.class));
    }

    @Test
    public void testDrainCompletedQueueEmpty()
    {
        operationConductor.drainPostProcessOperations();
    }

    @Test
    public void testDrainCompletedQueue()
    {
        addOperations(5, true, operationConductor.getRunningOperations());
        operationConductor.getPostProcessingOperationQueue().addAll(operationConductor.getRunningOperations());
        operationConductor.drainPostProcessOperations();
        Assert.assertEquals(operationConductor.getRunningOperations().size(), 0);
        Assert.assertEquals(operationConductor.getPostProcessingOperationQueue().size(), 0);
    }

    @Test
    public void testRun()
    {
        final int OP_COUNT = 10;
        addOperations(OP_COUNT, true, operationConductor.getPendingOperations());
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                OperationWrapper operationWrapper = (OperationWrapper)invocationOnMock.getArguments()[0];
                OnOperationCompleteListener onOperationCompleteListener = (OnOperationCompleteListener)invocationOnMock.getArguments()[2];
                onOperationCompleteListener.onComplete(operationWrapper);
                return null;
            }
        }).when(mockOperationRunnerFactory).createOperationRunner(any(OperationWrapper.class), any(ExecutorContext.class), any(OnOperationCompleteListener.class));
        operationConductor.run();
        Assert.assertEquals(operationConductor.getCompletedOperations().size(), OP_COUNT);
    }

    private Collection<String> addOperations(int count, boolean ready, Collection<OperationWrapper> listToAppend)
    {
        return IntStream.range(0, count).mapToObj(i->
        {
            String opId = Integer.toString(i);
            addOperation(opId, ready, listToAppend);
            return opId;
        }).collect(Collectors.toList());
    }


    private void addOperation(String operationName, boolean ready, Collection<OperationWrapper> listToAppend)
    {
        Operation operation = new Operation();
        operation.setName(operationName);
        OperationWrapper mockOperationWrapper = mock(OperationWrapper.class);
        doReturn(operation).when(mockOperationWrapper).getOperation();
        doReturn(ready).when(mockOperationWrapper).isReady(any(), any());
        listToAppend.add(mockOperationWrapper);
    }

}
