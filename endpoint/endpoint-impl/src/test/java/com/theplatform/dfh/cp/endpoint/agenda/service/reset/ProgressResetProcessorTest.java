package com.theplatform.dfh.cp.endpoint.agenda.service.reset;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.operation.OperationReference;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.api.progress.WaitingStateMessage;
import com.theplatform.dfh.cp.api.tokens.AgendaToken;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import com.comcast.fission.endpoint.api.ValidationException;
import com.comcast.fission.endpoint.api.agenda.ReigniteAgendaParameter;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProgressResetProcessorTest
{
    private static final String VALID_OP_NAME = "VALID";
    private ProgressResetProcessor progressResetProcessor;

    @BeforeMethod
    public void setup()
    {
        progressResetProcessor = new ProgressResetProcessor();
    }

    @Test
    public void testResetProgress()
    {
        Agenda agenda = createAgenda(Arrays.asList(
            createOperation("op.1"),
            createOperation("op.2")
        ));
        AgendaProgress agendaProgress = createAgendaProgressWithAgendaOps(
            ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), agenda);
        Map<ReigniteAgendaParameter, String> retryParameters = new HashMap<>();
        progressResetProcessor.resetProgress(agenda, agendaProgress, retryParameters);
        Assert.assertEquals(agendaProgress.getProcessingState(), ProcessingState.WAITING);
        Assert.assertEquals(agendaProgress.getProcessingStateMessage(), WaitingStateMessage.PENDING.toString());
        for(OperationProgress opProgress : agendaProgress.getOperationProgress())
        {
            Assert.assertEquals(opProgress.getProcessingState(), ProcessingState.WAITING);
            Assert.assertEquals(opProgress.getProcessingStateMessage(), WaitingStateMessage.PENDING.toString());
            Assert.assertEquals(opProgress.getResultPayload(), ProgressResetProcessor.RESET_OP_PAYLOAD);
        }
    }

    @Test
    public void testResetAgendaProgress()
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(ProcessingState.EXECUTING);
        agendaProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());
        progressResetProcessor.resetAgendaProgress(agendaProgress);
        Assert.assertEquals(agendaProgress.getProcessingState(), ProcessingState.WAITING);
        Assert.assertEquals(agendaProgress.getProcessingStateMessage(), WaitingStateMessage.PENDING.toString());
    }

    @Test
    public void testOperationProgress()
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(ProcessingState.EXECUTING);
        operationProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());
        progressResetProcessor.resetOperationProgress(operationProgress);
        Assert.assertEquals(operationProgress.getProcessingState(), ProcessingState.WAITING);
        Assert.assertEquals(operationProgress.getProcessingStateMessage(), WaitingStateMessage.PENDING.toString());
        Assert.assertEquals(operationProgress.getResultPayload(), ProgressResetProcessor.RESET_OP_PAYLOAD);
    }

    @Test
    public void getSpecifiedOperationsToReset()
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setOperation(VALID_OP_NAME);
        agendaProgress.setOperationProgress(new OperationProgress[] {operationProgress});

        String param = ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterNameWithValue(VALID_OP_NAME);

        Set<String> opsToReset = progressResetProcessor.getSpecifiedOperationsToReset(
            ReigniteAgendaParameter.getParametersMap(Collections.singletonList(param)),
            agendaProgress);

        Assert.assertEquals(opsToReset.size(), 1);
        Assert.assertTrue(opsToReset.contains(VALID_OP_NAME));
    }

    @DataProvider
    public Object[][] invalidSpecifiedOperationsToReset()
    {
        return new Object[][]
        {
            {null},
            {Arrays.asList("")},
            {Arrays.asList("invalid")},
            {Arrays.asList("invalid1","invalid2")},
        };
    }

    @Test(dataProvider = "invalidSpecifiedOperationsToReset", expectedExceptions = ValidationException.class)
    public void getSpecifiedOperationsToResetInvalid(List<String> opsToReset)
    {
        String param = opsToReset == null
                       ? ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterName()
                       : ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterNameWithValue(opsToReset);
        AgendaProgress agendaProgress = new AgendaProgress();
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setOperation(VALID_OP_NAME);
        agendaProgress.setOperationProgress(new OperationProgress[] {operationProgress});

        progressResetProcessor.getSpecifiedOperationsToReset(
            ReigniteAgendaParameter.getParametersMap(Collections.singletonList(param)),
            agendaProgress);
    }

    @DataProvider
    public Object[][] resetDependentOperationsProvider()
    {
        return new Object[][]
        {
            {
                Arrays.asList(
                    createOperation("1", null, null, null),
                    createOperation("2", new String[]{ "1", AgendaToken.AGENDA_ID.getToken() }, null, null),
                    createOperation("3", new String[] { "2" }, new String[] { "1" }, null), // reset as specified
                    createOperation("4", null, null, "2"),
                    createOperation("5", new String[] { "3" }, null, "4") // reset - 3
                ),
                Arrays.asList("3"),
                Arrays.asList("3", "5"),
                new ArrayList<>()
            },
            {
                Arrays.asList(
                    createOperation("1", null, null, null),
                    createOperation("2", new String[]{ "1", AgendaToken.AGENDA_ID.getToken() }, null, null), // reset as specified
                    createOperation("3", new String[] { "2" }, new String[] { "1" }, null), // reset - 2
                    createOperation("4", null, null, "2"), // deleted - 2
                    createOperation("5", new String[] { "3" }, null, "4") // deleted - 4
                ),
                Arrays.asList("2"),
                Arrays.asList("2", "3"),
                Arrays.asList("4", "5")
            },
            {
                Arrays.asList(
                    createOperation("1", null, null, null),
                    createOperation("2", new String[]{ "1", AgendaToken.AGENDA_ID.getToken() }, null, null), // reset as specified
                    createOperation("3", new String[] { "2" }, new String[] { "1" }, null), // reset - 2
                    createOperation("4", null, null, "2"), // deleted - 2 (part of requested reset list overriden)
                    createOperation("5", new String[] { "3" }, null, "4") // deleted - 4
                ),
                Arrays.asList("2", "4"),
                Arrays.asList("2", "3"),
                Arrays.asList("4", "5")
            },
            {
                Arrays.asList(
                    createOperation("1", null, null, null), // reset as specified
                    createOperation("2", new String[]{ "1", AgendaToken.AGENDA_ID.getToken() }, null, null), // reset - 1
                    createOperation("3", new String[] { "2" }, new String[] { "1" }, null), // reset - 2
                    createOperation("4", null, null, "2"), // deleted - 2 (part of requested reset list overriden)
                    createOperation("5", new String[] { "3" }, null, "4") // deleted - 4
                ),
                Arrays.asList("1"),
                Arrays.asList("1", "2", "3"),
                Arrays.asList("4", "5")
            },
            {
                Arrays.asList(
                    createOperation("1", null, null, null), // reset as specified
                    createOperation("2", new String[]{ "1", AgendaToken.AGENDA_ID.getToken() }, null, null), // reset - 1
                    createOperation("3", new String[] { "2" }, new String[] { "1" }, null), // reset - 2
                    createOperation("4", null, null, "2"), // deleted - 2 (part of requested reset list overriden)
                    createOperation("5", new String[] { "3" }, null, "4") // deleted - 4
                ),
                Arrays.asList("1", "2", "3", "4", "5"),
                Arrays.asList("1", "2", "3"),
                Arrays.asList("4", "5")
            }
        };
    }

    @Test(dataProvider = "resetDependentOperationsProvider")
    public void testResetDependentOperations(List<Operation> operations, List<String> explicitResetList, List<String> expectedOperationsToReset,
        List<String> expectedOperationsToDelete)
    {
        Agenda agenda = new Agenda();
        agenda.setOperations(operations);
        ProgressResetResult progressResetResult = progressResetProcessor.generateResetResult(agenda, new HashSet<>(explicitResetList));

        Assert.assertEquals(progressResetResult.getOperationsToReset().size(), expectedOperationsToReset.size(),
            String.format("Expected: [%1$s] Actual: [%2$s] -- Sizes: ", StringUtils.join(expectedOperationsToReset, ","),  StringUtils.join(progressResetResult.getOperationsToReset(), ",")));
        Assert.assertTrue(expectedOperationsToReset.containsAll(progressResetResult.getOperationsToReset()));

        Assert.assertEquals(progressResetResult.getOperationsToDelete().size(), expectedOperationsToDelete.size(),
            String.format("Expected: [%1$s] Actual: [%2$s] -- Sizes: ", StringUtils.join(expectedOperationsToDelete, ","),  StringUtils.join(progressResetResult.getOperationsToDelete(), ",")));
        Assert.assertTrue(expectedOperationsToDelete.containsAll(progressResetResult.getOperationsToDelete()));
    }

    /**
     * Creates operation with dependencies as specified
     * @param name Name of the op
     * @param references implicit references
     * @param dependsOn explicit references
     * @param generatedOperationParent operation parent name (this op is generated)
     * @return An operation configured accordingly
     */
    private Operation createOperation(String name, String[] references, String[] dependsOn, String generatedOperationParent)
    {
        JsonContext jsonContext = new JsonContext();
        JsonReferenceReplacer jref = jsonContext.getJsonReferenceReplacer();

        Operation operation = new Operation();
        operation.setName(name);
        Map<String, String> payloadObject = new HashMap<>();
        if(references != null && references.length > 0)
            Arrays.stream(references).forEach(ref ->
                payloadObject.put(UUID.randomUUID().toString(), jref.generateReference(ref + OperationReference.OUTPUT.getSuffix(), null)));
        operation.setPayload(payloadObject);

        ParamsMap paramsMap = new ParamsMap();
        if(dependsOn != null && dependsOn.length > 0)
            paramsMap.put(GeneralParamKey.dependsOn, StringUtils.join(dependsOn, ","));
        if(generatedOperationParent != null)
            paramsMap.put(GeneralParamKey.generatedOperationParent, generatedOperationParent);
        operation.setParams(paramsMap);
        return operation;
    }

    private Operation createOperation(String name)
    {
        Operation operation = createOperation(name, null, null, null);
        return operation;
    }

    private Agenda createAgenda(List<Operation> operations)
    {
        Agenda agenda = new Agenda();
        agenda.setOperations(operations);
        return agenda;
    }

    private AgendaProgress createAgendaProgressWithAgendaOps(ProcessingState processingState, String processingStateMessage, Agenda agenda)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setProcessingStateMessage(processingStateMessage);
        if(agenda.getOperations() != null)
        {
            agendaProgress.setOperationProgress(agenda.getOperations().stream().map(op ->
            {
                OperationProgress operationProgress = new OperationProgress();
                operationProgress.setOperation(op.getName());
                return operationProgress;
            }).collect(Collectors.toList()).toArray(new OperationProgress[]{}));
        }
        return agendaProgress;
    }

    private AgendaProgress createAgendaProgress(ProcessingState processingState, String processingStateMessage)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setProcessingStateMessage(processingStateMessage);
        return agendaProgress;
    }
}
