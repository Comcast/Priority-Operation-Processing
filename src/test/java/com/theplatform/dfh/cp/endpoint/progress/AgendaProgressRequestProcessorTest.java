package com.theplatform.dfh.cp.endpoint.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.query.ByFields;
import com.theplatform.dfh.endpoint.api.data.query.ById;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.query.Query;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

public class AgendaProgressRequestProcessorTest
{
    private static final String FIELD_1_NAME = "field1";
    private static final String FIELD_2_NAME = "field2";

    private AgendaProgressRequestProcessor requestProcessor;
    private ObjectPersister<AgendaProgress> mockAgendaProgressPersister;
    private ObjectPersister<Agenda> mockAgendaPersister;
    private ObjectPersister<OperationProgress> mockOperationProgressPersister;

    @BeforeMethod
    public void setup()
    {
        mockAgendaProgressPersister = mock(ObjectPersister.class);
        mockAgendaPersister = mock(ObjectPersister.class);
        mockOperationProgressPersister = mock(ObjectPersister.class);
        requestProcessor = new AgendaProgressRequestProcessor(mockAgendaProgressPersister, mockAgendaPersister, mockOperationProgressPersister);
    }

    @DataProvider
    public Object[][] shouldReturnFieldProvider()
    {
        return new Object[][]
            {
                {null, FIELD_1_NAME, true},
                { Collections.singletonList(new ByFields(FIELD_1_NAME)), FIELD_1_NAME, true},
                { Collections.singletonList(new ByFields(FIELD_1_NAME)), FIELD_2_NAME, false},
                { Collections.singletonList(new ById(UUID.randomUUID().toString())), FIELD_1_NAME, true},
                { Arrays.asList(new ByFields(FIELD_1_NAME), new ByFields(FIELD_2_NAME)), FIELD_2_NAME, true}
            };
    }

    @Test(dataProvider = "shouldReturnFieldProvider")
    public void testShouldReturnField(List<Query> queryList, String fieldName, final boolean EXPECTED_RESULT)
    {
        Assert.assertEquals(requestProcessor.shouldReturnField(new DefaultDataObjectRequest<>(queryList, null, null), fieldName), EXPECTED_RESULT);
    }

    @DataProvider
    public Object[][] agendaProgressAttemptsProvider()
    {
        return new Object[][]
            {
                {null, null, null},
                {null, createAgendaProgress(ProcessingState.COMPLETE, null), null},
                {createAgendaProgress(ProcessingState.COMPLETE, null), null, null},
                {createAgendaProgress(ProcessingState.COMPLETE, null), createAgendaProgress(ProcessingState.COMPLETE, null), null},
                {createAgendaProgress(ProcessingState.COMPLETE, null), createAgendaProgress(null, null), 1},
                {createAgendaProgress(ProcessingState.COMPLETE, null), createAgendaProgress(null, 2), 3}
            };
    }

    @Test(dataProvider = "agendaProgressAttemptsProvider")
    public void testUpdateAgendaProgressAttemptsOnComplete(AgendaProgress updatedProgress, AgendaProgress currentProgress, Integer expectedAttemptsCompleted)
    {
        requestProcessor.updateAgendaProgressAttemptsOnComplete(updatedProgress, currentProgress);
        if(updatedProgress != null)
            Assert.assertEquals(updatedProgress.getAttemptsCompleted(), expectedAttemptsCompleted);
    }

    private AgendaProgress createAgendaProgress(ProcessingState processingState, Integer attemptsCompleted)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setAttemptsCompleted(attemptsCompleted);
        return agendaProgress;
    }
}
