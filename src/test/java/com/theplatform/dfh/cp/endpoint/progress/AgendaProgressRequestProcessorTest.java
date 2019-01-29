package com.theplatform.dfh.cp.endpoint.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.facility.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

/**
 */
public class AgendaProgressRequestProcessorTest
{

    private AgendaProgressRequestProcessor agendaProgressRequestProcessor;
    private ObjectPersister<AgendaProgress> mockAgendaProgressPersister;
    private ObjectPersister<OperationProgress> mockOperationProgressPersister;
    private InsightSelector mockInsightSelector;

    @BeforeMethod
    void setUp()
    {
        mockAgendaProgressPersister = mock(ObjectPersister.class);
        mockOperationProgressPersister = mock(ObjectPersister.class);
        agendaProgressRequestProcessor = new AgendaProgressRequestProcessor(mockAgendaProgressPersister, mockOperationProgressPersister);
    }

    @Test
    void testErrorOnOpProgressUpdate()
    {
        // TODO
    }
}
