package com.comcast.pop.handler.executor.impl.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.tokens.AgendaToken;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.executor.BaseOperationExecutor;
import com.comcast.pop.modules.jsonhelper.replacement.ReferenceReplacementResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper for an operation. Handles readiness and tracking of success/failure (TODO).
 */
public class OperationWrapper
{
    private BaseOperationExecutor operationExecutor;
    private Set<String> dependencies = new HashSet<>();
    private Operation operation;
    private boolean ready;
    private String inputPayload;
    private String outputPayload;
    // These diagnostic events are for problems with the launching and tracking of operations, not the operations themselves (handled in the handlers)
    private List<DiagnosticEvent> diagnosticEvents;
    private boolean success = false;
    private Map<String, JsonNode> operationContextMap;
    private OperationProgress priorExecutionOperationProgress;

    public OperationWrapper(Operation operation)
    {
        this.operation = operation;
    }

    public OperationWrapper init(ExecutorContext executorContext, JsonContextReferenceParser jsonContextReferenceParser)
    {
        operationContextMap = new HashMap<>();
        operationContextMap.put(AgendaToken.AGENDA_ID.getToken(), new TextNode(executorContext.getAgendaId()));
        operationContextMap.put(AgendaToken.OPERATION_NAME.getToken(), new TextNode(operation.getName()));

        ReferenceReplacementResult referenceReplacementResult = evaluateReferences(executorContext);
        if(referenceReplacementResult.getMissingReferences() != null)
        {
            updateDependencies(referenceReplacementResult.getMissingReferences(), jsonContextReferenceParser);
        }
        updateReadiness(referenceReplacementResult);
        return this;
    }

    /**
     * Determines if all the dependencies are met for this operation
     * @return true if ready, false otherwise
     */
    public boolean isReady(ExecutorContext executorContext, Set<String> completedOperations)
    {
        if(completedOperations.containsAll(dependencies))
        {
            // promising... now try the actual reference replace
            if(updateReadiness(evaluateReferences(executorContext)))
            {
                return true;
            }
            else
            {
                throw new RuntimeException("All dependencies have been completed but there are still pending references.");
            }
        }
        return false;
    }

    /**
     * Runs the JsonContext process references checking for any invalid and returns the result
     * @param executorContext The context to pull the JsonContext from
     * @return The replacement results
     */
    private ReferenceReplacementResult evaluateReferences(ExecutorContext executorContext)
    {
        ReferenceReplacementResult result = executorContext.getJsonContext().processReferences(
            operation.getPayload(),
            Collections.singletonList(operationContextMap));
        Set<String> invalidReferences = result.getInvalidReferences();
        // TBD: this is BAD news (better exception and otherwise)
        if(invalidReferences != null && !invalidReferences.isEmpty())
        {
            throw new RuntimeException(String.format("Operation %1$s has invalid references: %2$s", operation.getName(),
                invalidReferences.stream().collect(Collectors.joining(","))));
        }
        return result;
    }

    /**
     * Updates the ready flag based on the result of the reference replacement
     * @param replacementResult The result of the reference replacement
     * @return true if ready, false otherwise
     */
    private boolean updateReadiness(ReferenceReplacementResult replacementResult)
    {
        if(ready) return true;

        if(replacementResult.getMissingReferences() != null && !replacementResult.getMissingReferences().isEmpty())
        {
            return false;
        }

        // all is well return the result
        ready = true;
        inputPayload = replacementResult.getResult();
        return true;
    }

    /**
     * Update the dependencies based on the missing references (future: include the dependsOn from the params)
     * @param missingReferences The set of missing references (fully specified)
     * @param jsonContextReferenceParser Necessary reference parser to map fully specified references to just the operation name
     */
    protected void updateDependencies(Set<String> missingReferences, JsonContextReferenceParser jsonContextReferenceParser)
    {
        dependencies.clear();

        if(missingReferences != null) dependencies.addAll(jsonContextReferenceParser.getOperationNames(missingReferences));

        if(operation.getParams() != null)
        {
            String dependsOn = operation.getParams().getString(GeneralParamKey.dependsOn.getKey());
            if(dependsOn != null)
            {
                String[] deps = dependsOn.split(",");
                Arrays.stream(deps).forEach(item ->
                {
                    String trimmedItem = item.trim();
                    if(!trimmedItem.isEmpty()) dependencies.add(item.trim());
                });
            }
        }
    }

    /**
     * Gets the processed payload
     * @return non-null if the payload is ready, null otherwise
     */
    public String getInputPayload()
    {
        return inputPayload;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public String getOutputPayload()
    {
        return outputPayload;
    }

    public void setOutputPayload(String outputPayload)
    {
        this.outputPayload = outputPayload;
    }

    /**
     * Gets a copied set of the dependencies
     * @return The copied set of dependencies
     */
    public Set<String> getDependencies()
    {
        return new HashSet<>(dependencies);
    }

    // unit test
    protected void setDependencies(Set<String> dependencies)
    {
        this.dependencies = dependencies;
    }

    public Boolean getSuccess()
    {
        return success;
    }

    public void setSuccess(Boolean success)
    {
        this.success = success;
    }

    public List<DiagnosticEvent> getDiagnosticEvents()
    {
        return diagnosticEvents;
    }

    /**
     * Adds a DiagnosticEvent to associate with this operation wrapper.
     * @param diagnosticEvent
     */
    public void addDiagnosticEvent(DiagnosticEvent diagnosticEvent)
    {
        if(diagnosticEvents == null) diagnosticEvents = new ArrayList<>();
        diagnosticEvents.add(diagnosticEvent);
    }

    public BaseOperationExecutor getOperationExecutor()
    {
        return operationExecutor;
    }

    public OperationWrapper setOperationExecutor(BaseOperationExecutor operationExecutor)
    {
        this.operationExecutor = operationExecutor;
        return this;
    }

    public OperationProgress getPriorExecutionOperationProgress()
    {
        return priorExecutionOperationProgress;
    }

    public void setPriorExecutionOperationProgress(OperationProgress priorExecutionOperationProgress)
    {
        this.priorExecutionOperationProgress = priorExecutionOperationProgress;
    }
}
