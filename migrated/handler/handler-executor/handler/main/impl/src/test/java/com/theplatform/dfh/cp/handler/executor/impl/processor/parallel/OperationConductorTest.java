package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.OperationRunnerFactory;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.AgendaProgressReporter;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import org.apache.commons.lang3.StringUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
    private AgendaProgressReporter mockAgendaProgressReporter;
    private JsonContext mockJsonContext;
    private LaunchDataWrapper mockLaunchDataWrapper;
    private ExecutorService mockExecutorService;

    @BeforeMethod
    public void setup()
    {
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        mockAgendaProgressReporter = mock(AgendaProgressReporter.class);
        mockExecutorContext = mock(ExecutorContext.class);
        doReturn(mockLaunchDataWrapper).when(mockExecutorContext).getLaunchDataWrapper();
        doReturn(mockAgendaProgressReporter).when(mockExecutorContext).getAgendaProgressReporter();
        mockJsonContext = mock(JsonContext.class);
        doReturn(mockJsonContext).when(mockExecutorContext).getJsonContext();
        mockExecutorService = mock(ExecutorService.class);
        mockOperationRunnerFactory = mock(OperationRunnerFactory.class);
        operationConductor = new OperationConductor(new ArrayList<>(), mockExecutorContext, null);
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
        // 1 pending op that is not ready and no running operations = DEADLOCK
        addOperations(1, false, operationConductor.getPendingOperations());
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

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Failed to execute operation.*")
    public void testLaunchReadyPendingOperationsExecutorServiceException()
    {
        addOperations(3, true, operationConductor.getPendingOperations());
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock)
            {
                throw new RuntimeException("ERROR");
            }
        }).when(mockExecutorService).submit(any(Runnable.class));

        operationConductor.launchReadyPendingOperations();
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

    @Test
    public void testPostProcessCompletedOperationFailed()
    {
        addOperations(5, true, operationConductor.getPendingOperations());
        OperationWrapper operationWrapper = operationConductor.getPendingOperations().get(0);
        operationWrapper.setSuccess(false);
        operationConductor.postProcessCompletedOperation(operationWrapper);
        Assert.assertEquals(operationConductor.getPendingOperations().size(), 0);
        Assert.assertTrue(operationConductor.hasExecutionFailed());
    }

    @Test
    public void testLoadPriorProgressNoURL()
    {
        // NOTE: this might change if the executor doesn't request the progress from the API
        operationConductor.loadPriorProgress();
    }

    @Test
    public void testLoadPriorProgressNull()
    {
        doReturn(null).when(mockLaunchDataWrapper).getLastProgressObject(any());
        operationConductor.loadPriorProgress();
    }

    @Test
    public void testLoadPriorProgressNoOpProgress()
    {
        doReturn(createAgendaProgress(null, null)).when(mockLaunchDataWrapper).getLastProgressObject(any());
        operationConductor.loadPriorProgress();
    }

    @Test
    public void testLoadPriorProgressWithOpProgress()
    {
        final List<String> operationNames = Arrays.asList("test.1", "test.2");
        doReturn(createAgendaProgress(operationNames, null)).when(mockLaunchDataWrapper).getLastProgressObject(any());
        operationConductor.setPendingOperations(createOperationWrappers(operationNames));
        operationConductor.loadPriorProgress();
        for (OperationWrapper operationWrapper : operationConductor.getPendingOperations())
        {
            Assert.assertEquals(operationWrapper.getOperation().getName(), operationWrapper.getPriorExecutionOperationProgress().getOperation());
        }
    }

    @DataProvider
    public Object[][] loadPriorProgressWithOpProgressStateProvider()
    {
        return new Object[][]
            {
                {ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), true},
                {ProcessingState.COMPLETE, "garbage", true},
                {ProcessingState.EXECUTING, "garbage", true},
                {ProcessingState.WAITING, "garbage", false},
            };
    }

    @Test(dataProvider = "loadPriorProgressWithOpProgressStateProvider")
    public void testLoadPriorProgressWithOpProgressState(ProcessingState processingState, String processingStateMessage, boolean expectPriorProgress)
    {
        final List<String> operationNames = Collections.singletonList("test.1");
        AgendaProgress agendaProgress = createAgendaProgress(operationNames, null);
        OperationProgress operationProgress = agendaProgress.getOperationProgress()[0];
        operationProgress.setId(OperationProgress.generateId(agendaProgress.getId(), operationNames.get(0)));
        operationProgress.setProcessingState(processingState);
        operationProgress.setProcessingStateMessage(processingStateMessage);
        doReturn(agendaProgress).when(mockLaunchDataWrapper).getLastProgressObject(any());
        operationConductor.setPendingOperations(createOperationWrappers(operationNames));
        operationConductor.loadPriorProgress();
        // ALWAYS should have the op present
        Assert.assertEquals(operationConductor.getPendingOperations().size(), 1);
        for (OperationWrapper operationWrapper : operationConductor.getPendingOperations())
        {
            Assert.assertEquals(operationWrapper.getPriorExecutionOperationProgress() != null, expectPriorProgress);
            if(expectPriorProgress)
                Assert.assertEquals(operationWrapper.getOperation().getName(), operationWrapper.getPriorExecutionOperationProgress().getOperation());
        }
    }

    @Test
    public void testLoadPriorProgressWithOpProgressMix()
    {
        final List<String> operationNames = Arrays.asList("test.1", "test.2", "test.3");
        final Set<String> suceededOperationNames = new HashSet<>(Arrays.asList("test.2"));
        doReturn(createAgendaProgress(operationNames, suceededOperationNames)).when(mockLaunchDataWrapper).getLastProgressObject(any());
        operationConductor.setPendingOperations(createOperationWrappers(operationNames));
        operationConductor.loadPriorProgress();
        for(String opName : operationNames)
        {
            boolean succededOp = suceededOperationNames.contains(opName);

            boolean foundPendingOp = operationConductor.getPendingOperations().stream()
                .anyMatch(operationWrapper ->
                    StringUtils.equals(operationWrapper.getOperation().getName(), opName));
            boolean foundSucceededOp = operationConductor.getCompletedOperations().stream()
                .anyMatch(operationWrapper ->
                    StringUtils.equals(operationWrapper.getOperation().getName(), opName));

            Assert.assertEquals(succededOp, foundSucceededOp, opName + (succededOp ? " should be in the succeeded set" : " should not be in the succeeded set"));
            Assert.assertEquals(!succededOp, foundPendingOp, opName + (!succededOp ? " should be in the pending set" : " should not be in the pending set"));
        }
    }

    @Test
    public void testGetFailedOperationsDelimitedNone()
    {
        Assert.assertEquals(operationConductor.getFailedOperationsDelimited(","), "");
    }

    @DataProvider
    public Object[][] getFailedOperationsDelimitedProvider()
    {
        return new Object[][]
        {
            {null, null, OperationConductor.UNKNOWN_OPERATION_NAME + "[" + OperationConductor.UNKNOWN_POD_NAME + "]"},
            {createOperation("Test"), null, "Test[" + OperationConductor.UNKNOWN_POD_NAME + "]"},
            {null, createMockOperationExecutor("theId"), OperationConductor.UNKNOWN_OPERATION_NAME + "[theId]"},
            {createOperation("Test"), createMockOperationExecutor("theId"), "Test[theId]"},
        };
    }

    @Test(dataProvider = "getFailedOperationsDelimitedProvider")
    public void testGetFailedOperationsDelimited(Operation operation, BaseOperationExecutor operationExecutor, final String EXPECTED_RESULT)
    {
        OperationWrapper operationWrapper = new OperationWrapper(operation);
        operationWrapper.setOperationExecutor(operationExecutor);
        operationConductor.setFailedOperations(Collections.singletonList(operationWrapper));
        Assert.assertEquals(operationConductor.getFailedOperationsDelimited(","), EXPECTED_RESULT);
    }

    protected Operation createOperation(String name)
    {
        Operation operation = new Operation();
        operation.setName(name);
        return operation;
    }

    protected BaseOperationExecutor createMockOperationExecutor(String identifier)
    {
        BaseOperationExecutor mockOperationExecutor = mock(BaseOperationExecutor.class);
        doReturn(identifier).when(mockOperationExecutor).getIdenitifier();
        return mockOperationExecutor;
    }

    protected List<OperationWrapper> createOperationWrappers(List<String> operationNames)
    {
        List<OperationWrapper> operationWrappers = new LinkedList<>();
        if(operationNames != null)
        {
            return operationNames.stream().map(opName ->
            {
                Operation operation = new Operation();
                operation.setName(opName);
                OperationWrapper operationWrapper = new OperationWrapper(operation);
                return operationWrapper;
            }).collect(Collectors.toList());
        }
        return operationWrappers;
    }

    protected AgendaProgress createAgendaProgress(List<String> operationNames, Set<String> succededOperationNames)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        if(operationNames != null)
        {
            agendaProgress.setOperationProgress(operationNames.stream().map(opName ->
            {
                OperationProgress opProgress = new OperationProgress();
                opProgress.setOperation(opName);
                if(succededOperationNames != null && succededOperationNames.contains(opName))
                {
                    opProgress.setProcessingState(ProcessingState.COMPLETE);
                    opProgress.setProcessingStateMessage(CompleteStateMessage.SUCCEEDED.toString());
                }
                return opProgress;
            }).collect(Collectors.toList()).toArray(new OperationProgress[0]));
        }
        return agendaProgress;
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
