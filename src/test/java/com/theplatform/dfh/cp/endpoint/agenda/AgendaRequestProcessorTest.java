package com.theplatform.dfh.cp.endpoint.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.scheduling.agenda.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaRequestProcessorTest
{
    private AgendaRequestProcessor agendaRequestProcessor;
    private ObjectPersister<Agenda> mockAgendaPersister;
    private ObjectPersister<ReadyAgenda> mockReadyAgendaPersister;
    private ObjectClient<AgendaProgress> mockAgendaProgressClient;
    private ObjectClient<OperationProgress> mockOperationProgressClient;
    private InsightSelector mockInsightSelector;

    @BeforeMethod
    void setUp()
    {
        mockAgendaPersister = mock(ObjectPersister.class);
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        mockAgendaProgressClient = mock(ObjectClient.class);
        mockOperationProgressClient = mock(ObjectClient.class);
        mockInsightSelector = mock(InsightSelector.class);
        agendaRequestProcessor = new AgendaRequestProcessor(mockAgendaPersister, mockReadyAgendaPersister, mockAgendaProgressClient, mockOperationProgressClient, mockInsightSelector);
        Mockito.when(mockInsightSelector.select(Mockito.any())).thenReturn(new Insight());
    }

    @Test
    void testHandlePost() throws PersistenceException
    {
        Operation operation1 = new Operation();
        operation1.setName(RandomStringUtils.randomAlphabetic(10));
        operation1.setPayload(RandomStringUtils.randomAlphanumeric(10));

        Operation operation2 = new Operation();
        operation2.setName(RandomStringUtils.randomAlphabetic(10));
        operation2.setPayload(RandomStringUtils.randomAlphanumeric(10));

        Agenda agenda = new Agenda();
        agenda.setCustomerId(UUID.randomUUID().toString());
        agenda.setLinkId(UUID.randomUUID().toString());
        agenda.setJobId(UUID.randomUUID().toString());
        agenda.setOperations(Arrays.asList(operation1, operation2));

        AgendaProgress agendaProgressResponse = new AgendaProgress();
        agendaProgressResponse.setId(UUID.randomUUID().toString());
        DataObjectResponse<AgendaProgress> dataObjectResponse = new DefaultDataObjectResponse<>();
        dataObjectResponse.add(agendaProgressResponse);
        doReturn(dataObjectResponse).when(mockAgendaProgressClient).persistObject(any());

        doReturn(new Agenda()).when(mockAgendaPersister).persist(any());

        DataObjectRequest request = new DefaultDataObjectRequest();
        ((DefaultDataObjectRequest) request).setDataObject(agenda);
        agendaRequestProcessor.handlePOST(request);

        verify(mockOperationProgressClient, times(2)).persistObject(any());
    }
}
