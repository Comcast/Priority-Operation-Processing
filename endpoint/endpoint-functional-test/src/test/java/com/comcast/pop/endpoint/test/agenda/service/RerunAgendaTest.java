package com.comcast.pop.endpoint.test.agenda.service;

import com.comcast.pop.endpoint.api.agenda.RerunAgendaParameter;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.test.base.EndpointTestBase;
import com.comcast.pop.endpoint.test.factory.DataGenerator;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.api.progress.WaitingStateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Test the rerun of Agendas
 */
public class RerunAgendaTest extends EndpointTestBase
{
    private static final Logger logger = LoggerFactory.getLogger(RerunAgendaTest.class);
    private final String OP_NAME_1 = "Log.1";
    private final String OP_NAME_2 = "Log.2";
    private final String OP_NAME_3 = "Log.3";

    @Test(enabled = false)
    public void simpleRerunAgenda()
    {
        final String AGENDA_ID = "86e2a373-9c16-4052-8580-a90ee5684962";
        List<String> params = Arrays.asList(
            RerunAgendaParameter.RESET_ALL.getParameterName()
            //,RerunAgendaParameter.SKIP_EXECUTION.getParameterName()
        );

        RerunAgendaResponse response = agendaServiceClient.rerunAgenda(new RerunAgendaRequest(AGENDA_ID, params));
        logger.info(jsonHelper.getPrettyJSONString(response));
    }



    @DataProvider
    public Object[][] hasResetALLParamsProvider()
    {
        return new Object[][]
                {
                        { true },
                        { false }
                };
    }

    @Test(dataProvider = "hasResetALLParamsProvider")
    public void testResetAll(boolean hasResetALLParams)
    {
        List<String> params = hasResetALLParams ? Collections.singletonList(RerunAgendaParameter.RESET_ALL.getParameterName()) : null;

        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2));
        adjustOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString());
        adjustOperationProgress(agenda, OP_NAME_2, ProcessingState.EXECUTING, "doing stuff");
        adjustAgendaProgress(agenda, ProcessingState.EXECUTING, "agenda doing stuff");

        verifyNoError(agendaServiceClient.rerunAgenda(new RerunAgendaRequest(agenda.getId(), params)));
        verifyAgendaProgress(agenda, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_1, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_2, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
    }

    @DataProvider
    public Object[][] resetSingleOpsProvider()
    {
        return new Object[][]
                {
                        { true},
                        { false}
                };
    }

    @Test(dataProvider = "resetSingleOpsProvider")
    public void testResetSpecificOps(boolean singleOp)
    {
        String resetOpNames = singleOp ? OP_NAME_2 : OP_NAME_2 + "," + OP_NAME_3;
        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2, OP_NAME_3));
        adjustOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString());
        adjustOperationProgress(agenda, OP_NAME_2, ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString());
        adjustOperationProgress(agenda, OP_NAME_3, ProcessingState.EXECUTING, "doing stuff");
        adjustAgendaProgress(agenda, ProcessingState.EXECUTING, "agenda doing stuff");

        verifyNoError(agendaServiceClient.rerunAgenda(new RerunAgendaRequest(agenda.getId(),
            Collections.singletonList(RerunAgendaParameter.OPERATIONS_TO_RESET.getParameterNameWithValue(resetOpNames))
            )));
        verifyAgendaProgress(agenda, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString());
        verifyOperationProgress(agenda, OP_NAME_2, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        if (singleOp)
        {
            verifyOperationProgress(agenda, OP_NAME_3, ProcessingState.EXECUTING, "doing stuff");
        }
        else
        {
            verifyOperationProgress(agenda, OP_NAME_3, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        }
    }

    @Test
    public void testContinueOps()
    {
        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2));
        adjustOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString());
        adjustOperationProgress(agenda, OP_NAME_2, ProcessingState.EXECUTING, "doing stuff");
        adjustAgendaProgress(agenda, ProcessingState.EXECUTING, "agenda doing stuff");

        verifyNoError(agendaServiceClient.rerunAgenda(new RerunAgendaRequest(agenda.getId(),
            Collections.singletonList(RerunAgendaParameter.CONTINUE.getParameterName())
        )));
        verifyAgendaProgress(agenda, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString());
        verifyOperationProgress(agenda, OP_NAME_2, ProcessingState.EXECUTING, "doing stuff");
    }

    @DataProvider
    public Object[][] skipExecutionStateProvider()
    {
        return new Object[][]
                {
                        { CompleteStateMessage.SUCCEEDED},
                        { CompleteStateMessage.FAILED}
                };
    }

    @Test(dataProvider = "skipExecutionStateProvider")
    public void testSkipExecutionAndResetAll(CompleteStateMessage state)
    {
        List<String> params = Arrays.asList(
            RerunAgendaParameter.SKIP_EXECUTION.getParameterName(),
                RerunAgendaParameter.RESET_ALL.getParameterName());

        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2, OP_NAME_3));
        adjustOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, state.toString());
        adjustOperationProgress(agenda, OP_NAME_2, ProcessingState.COMPLETE, state.toString());
        adjustOperationProgress(agenda, OP_NAME_3, ProcessingState.EXECUTING, "doing stuff");
        adjustAgendaProgress(agenda, ProcessingState.EXECUTING, "agenda doing stuff");

        verifyNoError(agendaServiceClient.rerunAgenda(
                new RerunAgendaRequest(agenda.getId(), params)));
        verifyAgendaProgress(agenda, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_1, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_2, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_3, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
    }

    @Test(dataProvider = "skipExecutionStateProvider")
    public void testSkipExecutionAndContinue(CompleteStateMessage state)
    {
        List<String> params = Arrays.asList(
            RerunAgendaParameter.SKIP_EXECUTION.getParameterName(),
                RerunAgendaParameter.CONTINUE.getParameterName());


        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2, OP_NAME_3));
        adjustOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, state.toString());
        adjustOperationProgress(agenda, OP_NAME_2, ProcessingState.COMPLETE, state.toString());
        adjustOperationProgress(agenda, OP_NAME_3, ProcessingState.EXECUTING, "doing stuff");
        adjustAgendaProgress(agenda, ProcessingState.EXECUTING, "agenda doing stuff");

        verifyNoError(agendaServiceClient.rerunAgenda(
                new RerunAgendaRequest(agenda.getId(), params)));
        verifyAgendaProgress(agenda, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, state.toString());
        verifyOperationProgress(agenda, OP_NAME_2, ProcessingState.COMPLETE, state.toString());
        verifyOperationProgress(agenda, OP_NAME_3, ProcessingState.EXECUTING, "doing stuff");
    }

    @Test(dataProvider = "skipExecutionStateProvider")
    public void testSkipExecutionAndResetSpecificOps(CompleteStateMessage state)
    {
        List<String> params = Arrays.asList(
            RerunAgendaParameter.SKIP_EXECUTION.getParameterName(),
                RerunAgendaParameter.OPERATIONS_TO_RESET.getParameterNameWithValue( OP_NAME_2 + "," + OP_NAME_3));


        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2, OP_NAME_3));
        adjustOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, state.toString());
        adjustOperationProgress(agenda, OP_NAME_2, ProcessingState.COMPLETE, state.toString());
        adjustOperationProgress(agenda, OP_NAME_3, ProcessingState.EXECUTING, "doing stuff");
        adjustAgendaProgress(agenda, ProcessingState.EXECUTING, "agenda doing stuff");

        verifyNoError(agendaServiceClient.rerunAgenda(
                new RerunAgendaRequest(agenda.getId(), params)));
        verifyAgendaProgress(agenda, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, state.toString());
        verifyOperationProgress(agenda, OP_NAME_2, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
        verifyOperationProgress(agenda, OP_NAME_3, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
    }

    @DataProvider
    public Object[][] invalidAgendaIdProvider()
    {
        return new Object[][]
                {
                        { null, 422, "ValidationException", "must be specified" },
                        { "", 422, "ValidationException",  "must be specified" },
                        { "garbageId", 404, "ObjectNotFoundException", "not found" }
                };
    }

    @Test(dataProvider = "invalidAgendaIdProvider")
    public void testRetryInvalidAgendaId(String agendaId, int responseCode, String expectedExceptionTitle, String expectedMessageFragment)
    {
        verifyError(agendaServiceClient.rerunAgenda(new RerunAgendaRequest(agendaId, null)), responseCode, expectedExceptionTitle, expectedMessageFragment);
    }

    @DataProvider
    public Object[][] invalidParamsNameProvider()
    {
        return new Object[][]
                {
                        { "garbageParamsName", 422, "ValidationException", "params is invalid" },
                        { "RESETALL", 422, "ValidationException", "params is invalid" },
                        { "OPERATIONSTORESET", 422, "ValidationException", "params is invalid" },
                        { "SKIPEXECUTION", 422, "ValidationException", "params is invalid" },
                        { "CONTINUE", 422, "ValidationException", "params is invalid" }
                };
    }

    // TODO: Declaring on 3-5-2020 that this may never be fixed/supported. The map is free form ... we certainly could validate everything in the map.
    @Test(dataProvider = "invalidParamsNameProvider", enabled = false)
    public void testRetryInvalidParamsName(String paramName, int responseCode, String expectedExceptionTitle, String expectedMessageFragment)
    {
        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2, OP_NAME_3));
        List<String> params = Collections.singletonList(paramName);

        verifyError(agendaServiceClient.rerunAgenda(new RerunAgendaRequest(agenda.getId(), params)), responseCode, expectedExceptionTitle, expectedMessageFragment);
    }


    @DataProvider
    public Object[][] invalidOperationsToResetProvider()
    {
        return new Object[][]
                {
                        { "operationsToReset", 422, "ValidationException", "params is invalid" },
                        { "operationsToReset=", 422, "ValidationException", "params is invalid" },
                        { "operationsToReset=null", 422, "ValidationException", "params is invalid" },
                        { "operationsToReset=nonExistentOP", 422, "ValidationException", "params is invalid" },
                        { String.format("operationsToReset=%1$s,nonExistentOP,%2$s", OP_NAME_1, OP_NAME_2), 422, "ValidationException", "params is invalid" },
// NOTE: this is tolerated
//                        { String.format("operationsToReset=%1$s,,%2$s", OP_NAME_1, OP_NAME_2), 422, "ValidationException", "params is invalid" }
                };
    }

    @Test(dataProvider = "invalidOperationsToResetProvider")
    public void testRetryInvalidParamsValue(String invalidOperationsToReset, int responseCode, String expectedExceptionTitle, String expectedMessageFragment)
    {
        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2, OP_NAME_3));
        List<String> params = Collections.singletonList(invalidOperationsToReset);

        verifyError(agendaServiceClient.rerunAgenda(new RerunAgendaRequest(agenda.getId(), params)), responseCode, expectedExceptionTitle, expectedMessageFragment);
    }

    @Test
    public void testRetryNullRequest()
    {
        verifyError(agendaServiceClient.rerunAgenda(null), 422, "ValidationException");
    }

    @DataProvider
    public Object[][] UnnecessaryParamsValueProvider()
    {
        return new Object[][]
                {
                        { String.format("continue=%1$s", OP_NAME_1)},
                        { String.format("resetAll=%1$s", OP_NAME_1)},
                        { String.format("skipExecution=%1$s", OP_NAME_1)},
                        { "continue=" },
                        { "resetAll=" },
                        { "skipExecution=" },
                        { "continue=null" },
                        { "resetAll=null" },
                        { "skipExecution=null" }
                };
    }

    @Test(dataProvider = "UnnecessaryParamsValueProvider")
    public void testRetryUnnecessaryParamsValue(String invalidOperationsToReset)
    {
        List<String> params = Collections.singletonList(invalidOperationsToReset);

        Agenda agenda = persistDataObject(agendaClient, Agenda.class, DataGenerator.createNonRunningLogAgenda(testCustomerId, OP_NAME_1, OP_NAME_2));
        adjustOperationProgress(agenda, OP_NAME_1, ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString());
        adjustOperationProgress(agenda, OP_NAME_2, ProcessingState.EXECUTING, "doing stuff");
        adjustAgendaProgress(agenda, ProcessingState.EXECUTING, "agenda doing stuff");

        verifyNoError(agendaServiceClient.rerunAgenda(new RerunAgendaRequest(agenda.getId(), params)));
        verifyAgendaProgress(agenda, ProcessingState.WAITING, WaitingStateMessage.PENDING.toString());
    }

    private void verifyAgendaProgress(Agenda agenda, ProcessingState processingState, String processingStateMessage)
    {
        DataObjectResponse<AgendaProgress> dataObjectResponse = agendaProgressClient.getObject(agenda.getProgressId());
        verifyNoError(dataObjectResponse);
        Assert.assertNotNull(dataObjectResponse.getFirst());
        AgendaProgress agendaProgress = dataObjectResponse.getFirst();
        Assert.assertEquals(agendaProgress.getProcessingState(), processingState);
        Assert.assertEquals(agendaProgress.getProcessingStateMessage(), processingStateMessage);
    }

    private void adjustAgendaProgress(Agenda agenda, ProcessingState processingState, String processingStateMessage)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setProcessingStateMessage(processingStateMessage);
        DataObjectResponse<AgendaProgress> dataObjectResponse =
                agendaProgressClient.updateObject(agendaProgress, agenda.getProgressId());
        verifyNoError(dataObjectResponse);
    }

    private void verifyOperationProgress(Agenda agenda, String opName, ProcessingState processingState, String processingStateMessage)
    {
        DataObjectResponse<OperationProgress> dataObjectResponse = operationProgressClient.getObject(OperationProgress.generateId(agenda.getProgressId(), opName));
        verifyNoError(dataObjectResponse);
        Assert.assertNotNull(dataObjectResponse.getFirst());
        OperationProgress operationProgress = dataObjectResponse.getFirst();
        Assert.assertEquals(operationProgress.getProcessingState(), processingState);
        Assert.assertEquals(operationProgress.getProcessingStateMessage(), processingStateMessage);
    }

    private void adjustOperationProgress(Agenda agenda, String opName, ProcessingState processingState, String processingStateMessage)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        operationProgress.setProcessingStateMessage(processingStateMessage);
        DataObjectResponse<OperationProgress> dataObjectResponse =
            operationProgressClient.updateObject(operationProgress, OperationProgress.generateId(agenda.getProgressId(), opName));
        verifyNoError(dataObjectResponse);
    }
}
