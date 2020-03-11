package com.theplatform.dfh.cp.handler.executor.impl.resident.generator;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.ResidentHandlerParams;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.processor.operation.generator.ResourcePoolAgendaUpdater;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaUpdateResidentHandlerTest
{
    final String OP_NAME = "theOp";
    private AgendaUpdateResidentHandler handler;
    private AgendaUpdateHandlerInput agendaUpdateHandlerInput;
    private ExecutorContext mockExecutorContext;
    private ResourcePoolAgendaUpdater mockResourcePoolAgendaUpdater;
    private ResourcePoolServiceClient mockResourcePoolServiceclient;
    private ResidentHandlerParams residentHandlerParams;
    private LaunchDataWrapper mockLaunchDataWrapper;
    private PropertyRetriever mockPropertyRetriever;
    private ProgressReporter mockProgressReporter;
    private Operation operation;

    @BeforeMethod
    public void setup()
    {
        operation = new Operation();
        operation.setName(OP_NAME);
        residentHandlerParams = new ResidentHandlerParams()
            .setOperation(operation);

        agendaUpdateHandlerInput = new AgendaUpdateHandlerInput();
        mockResourcePoolServiceclient = mock(ResourcePoolServiceClient.class);
        mockProgressReporter = mock(ProgressReporter.class);
        mockPropertyRetriever = mock(PropertyRetriever.class);
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        doReturn(mockPropertyRetriever).when(mockLaunchDataWrapper).getPropertyRetriever();
        mockExecutorContext = mock(ExecutorContext.class);
        doReturn(mockLaunchDataWrapper).when(mockExecutorContext).getLaunchDataWrapper();
        doReturn(mockResourcePoolServiceclient).when(mockExecutorContext).getResourcePoolServiceClient();
        mockResourcePoolAgendaUpdater = mock(ResourcePoolAgendaUpdater.class);
        handler = new AgendaUpdateResidentHandler(mockExecutorContext);
        handler.setResourcePoolAgendaUpdater(mockResourcePoolAgendaUpdater);
        handler.setProgressReporter(mockProgressReporter);
        handler.setResidentHandlerParams(residentHandlerParams);
    }

    @Test
    public void testSuccessfulUpdate()
    {
        doReturn(new ExpandAgendaResponse()).when(mockResourcePoolAgendaUpdater).update(any(), any());
        handler.execute(agendaUpdateHandlerInput);
        verify(mockResourcePoolAgendaUpdater, times(1)).update(any(), any());
    }

    // this use-case is just for local testing
    @Test
    public void testSuccessfulUpdateNoResourcePoolClient()
    {
        doReturn(null).when(mockExecutorContext).getResourcePoolServiceClient();
        handler.execute(agendaUpdateHandlerInput);
        verify(mockResourcePoolAgendaUpdater, times(0)).update(any(), any());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Failed to persist Agenda with generated operations.*")
    public void testErrorReponse()
    {
        ExpandAgendaResponse expandAgendaResponse = new ExpandAgendaResponse();
        expandAgendaResponse.setErrorResponse(new ErrorResponse());
        doReturn(expandAgendaResponse).when(mockResourcePoolAgendaUpdater).update(any(), any());
        handler.execute(agendaUpdateHandlerInput);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Failed to persist Agenda with generated operations.*")
    public void testNullResponse()
    {
        doReturn(null).when(mockResourcePoolAgendaUpdater).update(any(), any());
        handler.execute(agendaUpdateHandlerInput);
    }
}
