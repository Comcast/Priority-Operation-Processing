package com.comcast.pop.cp.endpoint.validation;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.operation.OperationReference;
import com.comcast.pop.api.tokens.AgendaToken;
import com.comcast.pop.endpoint.base.validation.DataObjectValidator;
import com.comcast.pop.modules.jsonhelper.replacement.JsonContext;
import com.comcast.pop.modules.jsonhelper.replacement.JsonReferenceReplacer;
import com.comcast.pop.modules.jsonhelper.replacement.ReferenceReplacementResult;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Agenda Validator
 *
 * Checks fields for glaring issues (customerId, references)
 */
public class AgendaValidator extends DataObjectValidator<Agenda, DataObjectRequest<Agenda>>
{
    private List<String> validationIssues;
    private JsonContext jsonContext = new JsonContext();
    private final int MAX_ISSUES = 10;

    @Override
    public void validatePOST(DataObjectRequest<Agenda> request)
    {
        super.validatePOST(request);
        validationIssues = new LinkedList<>();

        Agenda agenda = request.getDataObject();

        validateOperations(agenda);

        if(validationIssues.size() > 0)
        {
            int lastIssueIndex = Math.min(validationIssues.size(), MAX_ISSUES);
            throw new ValidationException(String.format("Validation Issues detected: %1$s%2$s",
                String.join(",", validationIssues.subList(0, lastIssueIndex)),
                lastIssueIndex < validationIssues.size() ? "[Truncating additional issues]" : "")
            );
        }
    }

    protected void validateOperations(Agenda agenda)
    {
        if(agenda.getOperations() == null || agenda.getOperations().size() == 0)
            throw new ValidationException("No operations specified in Agenda.");

        verifyUniqueOperationsName(agenda.getOperations());

        validateReferences(agenda);
    }

    protected void validateReferences(Agenda agenda)
    {
        if(agenda.getOperations() == null || agenda.getOperations().size() == 0)
            throw new ValidationException("No operations specified in Agenda.");

        JsonContext jsonContext = new JsonContext();
        // populate the context with temp data to validatePOST operation name references
        agenda.getOperations().forEach(op -> jsonContext.addData(op.getName() + OperationReference.OUTPUT.getSuffix(), "{}"));

        agenda.getOperations().forEach(op ->
        {
            if(op.getPayload() != null)
            {
                ReferenceReplacementResult result = jsonContext.processReferences(op.getPayload());
                // Can only check for missing. The invalid references check would require knowledge of the output payload format of every handler...
                if (result.getMissingReferences().size() > 0)
                {
                    // filter out pop tokens (TODO: if there is a need for other token filters this should be made more generic)
                    Set<String> filteredMissingReferences = filterNonPOPReferences(result.getMissingReferences());
                    if (filteredMissingReferences.size() > 0)
                    {
                        validationIssues.add(String.format(
                            "Invalid references found in operation [%1$s] payload: %2$s",
                            op.getName(),
                            String.join(",", result.getMissingReferences())));
                    }
                }
            }
        });

        if(validationIssues.size() == 0)
            checkForCircularReferences(agenda);
    }

    /**
     * Filters out any references that are pop tokens
     * @param references The references to filter
     * @return New set containing the non pop references
     */
    private Set<String> filterNonPOPReferences(Set<String> references)
    {
        return references
            .stream()
            .filter(ref -> !ref.startsWith(jsonContext.getJsonReferenceReplacer().getPrefix() + AgendaToken.TOKEN_PREFIX))
            .collect(Collectors.toSet());
    }

    protected void checkForCircularReferences(Agenda agenda)
    {
        JsonContext jsonContext = new JsonContext();
        JsonReferenceReplacer replacer = jsonContext.getJsonReferenceReplacer();

        Map<String, Set<String>> referenceMap = new HashMap<>();

        agenda.getOperations().forEach(op ->
        {
            if(op.getPayload() != null)
            {
                ReferenceReplacementResult result = jsonContext.processReferences(op.getPayload());
                referenceMap.put(op.getName(), result.getMissingReferences() == null
                                               ? new HashSet<>()
                                               : result.getMissingReferences().stream().map(ref ->
                    replacer.getReferenceName(ref).replace(OperationReference.OUTPUT.getSuffix(), ""))
                                                   .collect(Collectors.toSet()));
            }
        });

        agenda.getOperations().forEach(op ->
        {
            Set<String> operationsChecked = new HashSet<>();
            Stack<String> operationsToCheck = new Stack<>();
            operationsToCheck.push(op.getName());
            while(operationsToCheck.size() > 0)
            {
                String opName = operationsToCheck.pop();
                // clone from the reference map
                Set<String> referenceData = referenceMap.get(opName);
                // if there's no reference data the reference is not an operation in the Agenda
                if(referenceData == null) continue;
                Set<String> references = new HashSet<>(referenceData);
                // due to the structure of references we only check for a loop that returns to the original operation
                if(references.contains(op.getName()))
                {
                    validationIssues.add(String.format("There is a circular reference involving the operation: %1$s", op.getName()));
                    break;
                }
                operationsChecked.add(opName);
                // remove any previously checked operations
                references.removeAll(operationsChecked);
                operationsToCheck.addAll(references);
            }
        });
    }

    void verifyUniqueOperationsName(List<Operation> operations)
    {
        Set<String> opNames = new HashSet<>();
        for (Operation op : operations)
        {
            String opName = op.getName();
            if (opName == null || opName.isEmpty())
                throw new ValidationException("Operations must have names.");
            boolean unique = opNames.add(op.getName().toLowerCase());
            if (!unique)
                throw new ValidationException("Operation names must be unique.");
        }
    }

    protected List<String> getValidationIssues()
    {
        return validationIssues;
    }

    protected void setValidationIssues(List<String> validationIssues)
    {
        this.validationIssues = validationIssues;
    }

    public AgendaValidator setJsonContext(JsonContext jsonContext)
    {
        this.jsonContext = jsonContext;
        return this;
    }
}