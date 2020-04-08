package com.comcast.pop.handler.executor.impl.resident.generator;

import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comast.pop.handler.base.ResidentHandlerParams;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.processor.operation.generator.ResourcePoolAgendaUpdater;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaResponse;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClient;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UpdateAgendaResidentHandlerTest
{
    final String OP_NAME = "theOp";
    private UpdateAgendaResidentHandler handler;
    private UpdateAgendaHandlerInput updateAgendaHandlerInput;
    private ExecutorContext mockExecutorContext;
    private ResourcePoolAgendaUpdater mockResourcePoolAgendaUpdater;
    private ResourcePoolServiceClient mockResourcePoolServiceclient;
    private ResidentHandlerParams residentHandlerParams;
    private LaunchDataWrapper mockLaunchDataWrapper;
    private PropertyRetriever mockPropertyRetriever;
    private ProgressReporter mockProgressReporter;
    private Operation operation;
    private List<Operation> generatedOperations;

    @BeforeMethod
    public void setup()
    {
        operation = new Operation();
        operation.setName(OP_NAME);
        residentHandlerParams = new ResidentHandlerParams()
            .setOperation(operation);

        generatedOperations = IntStream.range(0, 10).mapToObj(i -> new Operation()).collect(Collectors.toList());
        updateAgendaHandlerInput = new UpdateAgendaHandlerInput();
        updateAgendaHandlerInput.setOperations(generatedOperations);

        mockResourcePoolServiceclient = mock(ResourcePoolServiceClient.class);
        mockProgressReporter = mock(ProgressReporter.class);
        mockPropertyRetriever = mock(PropertyRetriever.class);
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        doReturn(mockPropertyRetriever).when(mockLaunchDataWrapper).getPropertyRetriever();
        mockExecutorContext = mock(ExecutorContext.class);
        doReturn(mockLaunchDataWrapper).when(mockExecutorContext).getLaunchDataWrapper();
        doReturn(mockResourcePoolServiceclient).when(mockExecutorContext).getResourcePoolServiceClient();
        mockResourcePoolAgendaUpdater = mock(ResourcePoolAgendaUpdater.class);
        handler = new UpdateAgendaResidentHandler(mockExecutorContext);
        handler.setResourcePoolAgendaUpdater(mockResourcePoolAgendaUpdater);
        handler.setProgressReporter(mockProgressReporter);
        handler.setResidentHandlerParams(residentHandlerParams);
    }

    @Test
    public void testSuccessfulUpdate()
    {
        doReturn(new UpdateAgendaResponse()).when(mockResourcePoolAgendaUpdater).update(any(), any());
        handler.execute(updateAgendaHandlerInput);
        verify(mockResourcePoolAgendaUpdater, times(1)).update(any(), any());
        verifyOperationsAdjusted();
    }

    // this use-case is just for local testing
    @Test
    public void testSuccessfulUpdateNoResourcePoolClient()
    {
        doReturn(null).when(mockExecutorContext).getResourcePoolServiceClient();
        handler.execute(updateAgendaHandlerInput);
        verify(mockResourcePoolAgendaUpdater, times(0)).update(any(), any());
        verifyOperationsAdjusted();
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Failed to persist Agenda with generated operations.*")
    public void testErrorReponse()
    {
        UpdateAgendaResponse expandAgendaResponse = new UpdateAgendaResponse();
        expandAgendaResponse.setErrorResponse(new ErrorResponse());
        doReturn(expandAgendaResponse).when(mockResourcePoolAgendaUpdater).update(any(), any());
        handler.execute(updateAgendaHandlerInput);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Failed to persist Agenda with generated operations.*")
    public void testNullResponse()
    {
        doReturn(null).when(mockResourcePoolAgendaUpdater).update(any(), any());
        handler.execute(updateAgendaHandlerInput);
    }

    private void verifyOperationsAdjusted()
    {
        Assert.assertFalse(generatedOperations.stream().anyMatch(op ->
            op.getParams() == null
                || !op.getParams().containsKey(GeneralParamKey.generatedOperationParent)
                || !StringUtils.equalsIgnoreCase(op.getParams().getString(GeneralParamKey.generatedOperationParent), OP_NAME)));
    }
}
