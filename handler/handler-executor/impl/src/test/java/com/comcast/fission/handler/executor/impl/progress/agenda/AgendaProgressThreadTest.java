package com.comcast.fission.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaProgressThreadTest
{
    private AgendaProgressThread agendaProgressThread;
    private AgendaProgressThreadConfig mockAgendaProgressThreadConfig;
    private AgendaProgressFactory agendaProgressFactory;
    private ProgressReporter mockReporter;

    @BeforeMethod
    public void setup()
    {
        mockReporter = mock(ProgressReporter.class);
        agendaProgressFactory = new AgendaProgressFactory("");
        mockAgendaProgressThreadConfig = mock(AgendaProgressThreadConfig.class);

        doReturn(mockReporter).when(mockAgendaProgressThreadConfig).getReporter();

    }

    @DataProvider
    public Object[][] updateProgressItemsToReportProvider()
    {
        return new Object[][]
            {
                {ProcessingState.EXECUTING, 1, 1},
                {ProcessingState.COMPLETE, 0, 1},
                {null, 1, 0} // no items to report, bad state
            };
    }

    @Test(dataProvider = "updateProgressItemsToReportProvider")
    public void testUpdateProgressItemsToReport(ProcessingState processingState, int expectedRetrieverSize, int expectedProgressItemsToReport)
    {
        Set<OperationProgressProvider> retrieverSet = createOperationProgressRetrievers(1);
        OperationProgressProvider operationProgressProvider = (OperationProgressProvider)retrieverSet.toArray()[0];
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        doReturn(operationProgress).when(operationProgressProvider).retrieveOperationProgress();

        agendaProgressThread = new AgendaProgressThread(mockAgendaProgressThreadConfig);
        agendaProgressThread.setOperationProgressProviders(retrieverSet);

        agendaProgressThread.updateProgressItemsToReport();

        Assert.assertEquals(agendaProgressThread.getOperationProgressProviders().size(), expectedRetrieverSize);
        Assert.assertEquals(agendaProgressThread.getOperationProgressToReport().size(), expectedProgressItemsToReport);
    }

    @Test
    public void testNullProgressToReport()
    {
        Set<OperationProgressProvider> retrieverSet = createOperationProgressRetrievers(1);
        OperationProgressProvider operationProgressProvider = (OperationProgressProvider)retrieverSet.toArray()[0];
        doReturn(null).when(operationProgressProvider).retrieveOperationProgress();

        agendaProgressThread = new AgendaProgressThread(mockAgendaProgressThreadConfig);
        agendaProgressThread.setOperationProgressProviders(retrieverSet);

        agendaProgressThread.updateProgressItemsToReport();

        // A null return from a provider should have no impact
        Assert.assertEquals(agendaProgressThread.getOperationProgressProviders().size(), 1);
    }

    @Test
    public void testMultipleProgressToReportOnSameOperation()
    {
        final String OPERATION_NAME = "thePlatform.encode.1";
        Set<OperationProgressProvider> retrieverSet = createOperationProgressRetrievers(1);
        OperationProgressProvider operationProgressProvider = (OperationProgressProvider)retrieverSet.toArray()[0];
        OperationProgress operationProgress = createOperationProgress(ProcessingState.EXECUTING, OPERATION_NAME);

        doReturn(operationProgress).when(operationProgressProvider).retrieveOperationProgress();

        agendaProgressThread = new AgendaProgressThread(mockAgendaProgressThreadConfig);

        // put in an existing progress with the same op name (singleton list doesn't support removeIf)
        List<OperationProgress> operationProgresses = new LinkedList<>();
        operationProgresses.add(createOperationProgress(ProcessingState.EXECUTING, OPERATION_NAME));
        agendaProgressThread.setOperationProgressToReport(operationProgresses);

        agendaProgressThread.setOperationProgressProviders(retrieverSet);

        agendaProgressThread.updateProgressItemsToReport();

        Assert.assertEquals(agendaProgressThread.getOperationProgressToReport().size(), 1);
        Assert.assertEquals(operationProgress, agendaProgressThread.getOperationProgressToReport().get(0));

        // A null return from a provider should have no impact
        Assert.assertEquals(agendaProgressThread.getOperationProgressProviders().size(), 1);
    }

    @Test
    public void testReportProgressOnNoOps()
    {
        agendaProgressThread = new AgendaProgressThread(mockAgendaProgressThreadConfig);
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                AgendaProgress agendaProgress = (AgendaProgress)invocationOnMock.getArguments()[0];
                Assert.assertNull(agendaProgress.getOperationProgress());
                Assert.assertEquals(agendaProgress.getProcessingState(), ProcessingState.COMPLETE);
                return null;
            }
        }).when(mockReporter).reportProgress(any());
        setAgendaProgress(ProcessingState.COMPLETE, agendaProgressThread);
        Assert.assertTrue(agendaProgressThread.isThereProgressToReport());
        agendaProgressThread.reportProgress();
        Assert.assertFalse(agendaProgressThread.isThereProgressToReport());
        verify(mockReporter, times(1)).reportProgress(any());
    }

    @Test
    public void testReportProgressWithOpProgress()
    {
        agendaProgressThread = new AgendaProgressThread(mockAgendaProgressThreadConfig);
        agendaProgressThread.setOperationProgressProviders(createOperationProgressRetrievers(1));
        agendaProgressThread.setOperationProgressToReport(createOperationProgressItems(1));
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                AgendaProgress agendaProgress = (AgendaProgress)invocationOnMock.getArguments()[0];
                Assert.assertEquals(agendaProgress.getOperationProgress().length, 1);
                Assert.assertEquals(agendaProgress.getProcessingState(), ProcessingState.EXECUTING);
                return null;
            }
        }).when(mockReporter).reportProgress(any());
        setAgendaProgress(ProcessingState.EXECUTING, agendaProgressThread);
        agendaProgressThread.reportProgress();
        verify(mockReporter, times(1)).reportProgress(any());

    }

    @DataProvider
    public Object[][] calculatePercentCompleteProvider()
    {
        return new Object[][]
            {
                {0, 1, 0d, 0d},
                {1, 0, 100d, 0d},
                {5, 10, 50d, 55d},
                {5, 10, 0d, 50d},
                {5, 10, 100d, 60d}
            };
    }

    @Test(dataProvider = "calculatePercentCompleteProvider")
    public void testCalculatePercentComplete(int completedOperationCount, int totalOperationCount, double incompleteOperationPercentTotal, double EXPECTED_VALUE)
    {
        Assert.assertEquals(AgendaProgressThread.calculatePercentComplete(completedOperationCount, totalOperationCount, incompleteOperationPercentTotal), EXPECTED_VALUE);
    }

    private void setAgendaProgress(ProcessingState processingState, AgendaProgressThread agendaProgressThread)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(UUID.randomUUID().toString());
        agendaProgress.setProcessingState(processingState);
        agendaProgressThread.setAgendaProgress(agendaProgress);
    }

    protected List<OperationProgress> createOperationProgressItems(int count)
    {
        List<OperationProgress> operationProgressList = new ArrayList<>(count);
        IntStream.range(0, count).forEach(i ->
            operationProgressList.add(mock(OperationProgress.class))
        );
        return operationProgressList;
    }

    protected Set<OperationProgressProvider> createOperationProgressRetrievers(int count)
    {
        HashSet<OperationProgressProvider> opRetrieverSet = new HashSet<>(count);
        IntStream.range(0, count).forEach(i ->
            opRetrieverSet.add(mock(OperationProgressProvider.class))
        );
        return opRetrieverSet;
    }

    protected OperationProgress createOperationProgress(ProcessingState processingState, String name)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setOperation(name);
        operationProgress.setProcessingState(processingState);
        return operationProgress;
    }
}
