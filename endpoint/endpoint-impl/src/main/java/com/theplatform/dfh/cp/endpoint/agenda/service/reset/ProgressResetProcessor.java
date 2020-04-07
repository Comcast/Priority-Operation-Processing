package com.theplatform.dfh.cp.endpoint.agenda.service.reset;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.operation.OperationReference;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.api.progress.WaitingStateMessage;
import com.comcast.pop.api.tokens.AgendaToken;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceComponents;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.ReferenceReplacementResult;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaParameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Processes the reset of the Agenda and OperationProgress for the retry endpoint
 */
public class ProgressResetProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(ProgressResetProcessor.class);
    protected static final String RESET_OP_PAYLOAD = "{}";

    public ProgressResetResult resetProgress(Agenda agenda, AgendaProgress agendaProgress, Map<ReigniteAgendaParameter, String> retryParameters)
    {

        boolean resetAll = retryParameters.containsKey(ReigniteAgendaParameter.RESET_ALL);
        boolean continueOnly = retryParameters.containsKey(ReigniteAgendaParameter.CONTINUE);
        Set<String> operationsToReset = getSpecifiedOperationsToReset(retryParameters, agendaProgress);

        if (!resetAll
            && operationsToReset.size() == 0
            && !continueOnly)
        {
            // nothing was specified so default to resetting everything
            resetAll = true;
        }

        if(resetAll)
        {
            operationsToReset = getAllOperations(agenda);
        }

        resetAgendaProgress(agendaProgress);

        //logger.info("operationsToReset: {}", operationsToReset == null ? "null" : StringUtils.join(operationsToReset, ";"));

        // gather up the information on the final ops to reset (includes dependencies and potentially removed ops due to generation)
        ProgressResetResult progressResetResult = generateResetResult(agenda, operationsToReset);
        // reset the OperationProgress objects in the AgendaProgress
        resetOperationProgresses(agendaProgress, progressResetResult.getOperationsToReset());
        return progressResetResult;
    }

    protected void resetOperationProgresses(AgendaProgress agendaProgress, final Set<String> operationsToReset)
    {
        if (agendaProgress.getOperationProgress() == null)
            return;

        Arrays.stream(agendaProgress.getOperationProgress())
            .filter(op -> operationsToReset.contains(op.getOperation()))
            .forEach(this::resetOperationProgress);
    }

    private Set<String> getAllOperations(Agenda agenda)
    {
        return agenda.getOperations().stream().map(Operation::getName).collect(Collectors.toSet());
    }

    protected void resetAgendaProgress(AgendaProgress agendaProgress)
    {
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        agendaProgress.setProcessingStateMessage(WaitingStateMessage.PENDING.toString());
    }

    protected void resetOperationProgress(OperationProgress operationProgress)
    {
        operationProgress.setProcessingState(ProcessingState.WAITING);
        operationProgress.setProcessingStateMessage(WaitingStateMessage.PENDING.toString());
        // TODO: cannot specify null (very annoying)
        operationProgress.setResultPayload(RESET_OP_PAYLOAD);
        operationProgress.setDiagnosticEvents(new DiagnosticEvent[]{});
        operationProgress.setPercentComplete(0d);
    }

    /**
     * Evaluates all the operations of an Agenda to see if they need to be reset or deleted.
     * @param agenda The agenda (should have all ops!)
     * @param specifiedResetOperationNames The known operations to reset (as specified by the caller of the reset method)
     * @return A ProgressResetResult specifying the operations to reset and to delete
     */
    protected ProgressResetResult generateResetResult(Agenda agenda, Set<String> specifiedResetOperationNames)
    {
        ProgressResetResult progressResetResult = new ProgressResetResult();
        Set<String> operationsToReset = new HashSet<>(specifiedResetOperationNames);
        Map<String, OperationDependencies> operationDependencyMap = buildOperationDependencyMap(agenda);
        Map<String, OperationDependencies> workingOperationDependencyMap = new HashMap<>(operationDependencyMap);

        for(String opName : operationsToReset)
        {
            // any declared reset ops that were generated by other reset ops are removed
            OperationDependencies operationDependencies = operationDependencyMap.get(opName);
            if(operationsToReset.contains(operationDependencies.getGeneratedParent()))
            {
                progressResetResult.getOperationsToDelete().add(opName);
            }
            // remove any of the known reset operations from further evaluation
            workingOperationDependencyMap.remove(opName);
        }

        // Loop through the "remaining" operations to see if they have a dependency or were generated by a reset operation
        // This is a repeating process until no operations are affected.
        while(true)
        {
            if(workingOperationDependencyMap.isEmpty())
            {
                // EXIT POINT - no operations remain to be evaluated (all reset)
                break;
            }

            Set<String> additionalOperationsToReset = new HashSet<>();
            for(Map.Entry<String, OperationDependencies> entry : workingOperationDependencyMap.entrySet())
            {
                OperationDependencies opDependencies = entry.getValue();
                if(opDependencies.getDependencies().stream().anyMatch(operationsToReset::contains))
                {
                    // TODO: this is not good enough -- might remove an op that should be deleted
                    additionalOperationsToReset.add(entry.getKey());
                }
                if(operationsToReset.contains(opDependencies.getGeneratedParent()))
                {
                    progressResetResult.getOperationsToDelete().add(entry.getKey());
                    // NOTE: We mark deleted operations as reset operations as well for evaluation
                    additionalOperationsToReset.add(entry.getKey());
                }
            }

            if(additionalOperationsToReset.isEmpty())
            {
                // EXIT POINT - no additional operations need to be reset (none found by this iteration)
                break;
            }

            operationsToReset.addAll(additionalOperationsToReset);

            // remove any of the known reset operations from further the evaluation
            for(String opName : additionalOperationsToReset)
            {
                workingOperationDependencyMap.remove(opName);
            }
        }

        // one final post process to move any resets that should be deletes over (does not impact any further nesting)
        for(String opName : operationsToReset)
        {
            OperationDependencies operationDependencies = operationDependencyMap.get(opName);
            // all generators with a parent that was reset should be migrated to delete
            if(operationsToReset.contains(operationDependencies.getGeneratedParent()))
            {
                progressResetResult.getOperationsToDelete().add(opName);
            }
        }

        // operations that are to be deleted do not need to be reset
        operationsToReset.removeAll(progressResetResult.getOperationsToDelete());
        progressResetResult.getOperationsToReset().addAll(operationsToReset);

        return progressResetResult;
    }

    /**
     * Builds a map of operation names to OperationDependencies (dependencies + parent-generator name)
     * @param agenda The agenda to extract the operation details/dependencies from
     * @return Map of operation names to OperationDependencies
     */
    protected Map<String, OperationDependencies> buildOperationDependencyMap(Agenda agenda)
    {
        Map<String, OperationDependencies> operationDependencyMap = new HashMap<>();

        JsonContext jsonContext = new JsonContext();
        // populate the context with temp data to validatePOST operation name references
        // agenda.getOperations().forEach(op -> jsonContext.addData(op.getName() + OperationReference.OUTPUT.getSuffix(), "{}"));

        agenda.getOperations().forEach(op ->
        {
            OperationDependencies operationDependencies = new OperationDependencies();

            Set<String> dependencies = operationDependencies.getDependencies();
            operationDependencyMap.put(op.getName(), operationDependencies);

            // get explicit dependencies
            if(op.getParams() != null && op.getParams().containsKey(GeneralParamKey.dependsOn))
            {
                String[] dependsOnItems = StringUtils.split(op.getParams().getString(GeneralParamKey.dependsOn), ",");
                dependencies.addAll(Arrays.asList(dependsOnItems));
            }

            // get implicit dependencies
            if(op.getPayload() != null)
            {
                ReferenceReplacementResult result = jsonContext.processReferences(op.getPayload());
                if (result.getMissingReferences().size() > 0)
                {
                    Set<String> filteredMissingReferences = filterNonFissionReferences(jsonContext, result.getMissingReferences());
                    // TODO: if there ever other types of op references this will be wrong
                    // op references have out appended
                    dependencies.addAll(filteredMissingReferences.stream()
                        .map(ref -> StringUtils.removeEnd(ref, OperationReference.OUTPUT.getSuffix()))
                        .collect(Collectors.toList()));
                }
            }

            // get generated operation parent
            if(op.getParams() != null)
            {
                operationDependencies.setGeneratedParent(
                    op.getParams().getString(GeneralParamKey.generatedOperationParent, null));
            }
        });
//        System.out.println(new JsonHelper().getPrettyJSONString(operationDependencyMap));
        return operationDependencyMap;
    }

    /**
     * Filters out any references that are fission tokens
     * @param references The references to filter
     * @return New set containing the non fission references
     */
    private Set<String> filterNonFissionReferences(JsonContext jsonContext, Set<String> references)
    {
        JsonReferenceReplacer refReplacer = jsonContext.getJsonReferenceReplacer();
        // TODO: consider just having a full list of non-op references instead of just checking for the TOKEN_PREFIX
        // map all the references that are not fission tokens to operation names (using the JsonReferenceReplacer.extractComponents)
        return references
            .stream()
            .map(ref -> {
                JsonReferenceComponents components = refReplacer.extractComponents(ref);
                if(components != null)
                    return components.getParameter();
                else
                    return ref;
            })
            .filter(ref ->
                !StringUtils.startsWith(ref, AgendaToken.AGENDA_ID.getToken())
                 && !StringUtils.startsWith(ref, AgendaToken.OPERATION_NAME.getToken()))
            .collect(Collectors.toSet());
    }


    protected Set<String> getSpecifiedOperationsToReset(Map<ReigniteAgendaParameter, String> retryParameters, AgendaProgress agendaProgress)
    {
        if (!retryParameters.containsKey(ReigniteAgendaParameter.OPERATIONS_TO_RESET))
            return new HashSet<>();

        // validate the incoming ops to reset are valid (requires the AgendaProgress so this cannot be performed up front)
        String delimitedOps = retryParameters.get(ReigniteAgendaParameter.OPERATIONS_TO_RESET);
        String[] opsToReset = StringUtils.split(delimitedOps, ReigniteAgendaParameter.VALUE_DELIMITER);
        if (opsToReset == null || opsToReset.length == 0)
            throw new ValidationException(String.format("params is invalid - %1$s has no operations specified", ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterName()));
        Set<String> operationNames = Arrays.stream(agendaProgress.getOperationProgress()).map(OperationProgress::getOperation).collect(Collectors.toSet());
        List<String> invalidOpNames = Arrays.stream(opsToReset).filter(resetOpName -> !operationNames.contains(resetOpName)).collect(Collectors.toList());
        if (invalidOpNames.size() > 0)
            throw new ValidationException(String.format(
                "params is invalid - %1$s has the following invalid operation names: %2$s",
                ReigniteAgendaParameter.OPERATIONS_TO_RESET.getParameterName(),
                String.join(",", invalidOpNames)));

        return Arrays.stream(opsToReset).collect(Collectors.toSet());
    }

    private static class OperationDependencies
    {
        private Set<String> dependencies = new HashSet<>();
        private String generatedParent = null;

        public Set<String> getDependencies()
        {
            return dependencies;
        }

        public void setDependencies(Set<String> dependencies)
        {
            this.dependencies = dependencies;
        }

        public String getGeneratedParent()
        {
            return generatedParent;
        }

        public void setGeneratedParent(String generatedParent)
        {
            this.generatedParent = generatedParent;
        }
    }
}
