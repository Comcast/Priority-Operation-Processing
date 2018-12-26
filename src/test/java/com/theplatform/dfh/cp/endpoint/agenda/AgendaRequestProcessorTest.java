package com.theplatform.dfh.cp.endpoint.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.scheduling.agenda.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private HttpCPObjectClient<AgendaProgress> mockAgendaProgressClient;
    private HttpCPObjectClient<OperationProgress> mockOperationProgressClient;
    private InsightSelector mockInsightSelector;

    @BeforeMethod
    void setUp()
    {
        mockAgendaPersister = mock(ObjectPersister.class);
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        mockAgendaProgressClient = mock(HttpCPObjectClient.class);
        mockOperationProgressClient = mock(HttpCPObjectClient.class);
        mockInsightSelector = mock(InsightSelector.class);
        agendaRequestProcessor = new AgendaRequestProcessor(mockAgendaPersister, mockReadyAgendaPersister, mockAgendaProgressClient, mockOperationProgressClient, mockInsightSelector);
        Mockito.when(mockInsightSelector.select(Mockito.any())).thenReturn(new Insight());
    }

    @Test
    void testHandlePost() throws BadRequestException
    {
        Operation operation1 = new Operation();
        operation1.setName(RandomStringUtils.randomAlphabetic(10));
        operation1.setPayload(RandomStringUtils.randomAlphanumeric(10));

        Operation operation2 = new Operation();
        operation2.setName(RandomStringUtils.randomAlphabetic(10));
        operation2.setPayload(RandomStringUtils.randomAlphanumeric(10));

        Agenda agenda = new Agenda();
        agenda.setLinkId(UUID.randomUUID().toString());
        agenda.setJobId(UUID.randomUUID().toString());
        agenda.setOperations(Arrays.asList(operation1, operation2));

        ObjectPersistResponse agendaProgressResponse = new ObjectPersistResponse();
        agendaProgressResponse.setId(UUID.randomUUID().toString());
        doReturn(agendaProgressResponse).when(mockAgendaProgressClient).persistObject(any());

        agendaRequestProcessor.handlePOST(agenda);

        verify(mockOperationProgressClient, times(2)).persistObject(any());
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Operation names must be unique.")
    void testDuplicateOperationNames()
    {
        Operation op1 = new Operation();
        op1.setName("foo");
        Operation op2 = new Operation();
        op2.setName("FOo");
        List<Operation> operations = new ArrayList<>();
        operations.add(op1);
        operations.add(op2);

        agendaRequestProcessor.verifyUniqueOperationsName(operations);
    }
}
