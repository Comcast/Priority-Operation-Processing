package com.theplatform.dfh.cp.endpoint.agenda;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.ReferenceReplacementResult;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.apache.commons.lang3.StringUtils;

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
public class AgendaValidator
{
    private JsonHelper jsonHelper = new JsonHelper();
    private List<String> validationIssues;
    private final int MAX_ISSUES = 10;

    public void validate(Agenda agenda)
    {
        validationIssues = new LinkedList<>();

        if(StringUtils.isBlank(agenda.getCustomerId()))
            throw new ValidationException("The customer id must be specified on the agenda.");

        JsonNode rootTransformNode = jsonHelper.getObjectMapper().valueToTree(agenda);

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
        // populate the context with temp data to validate operation name references
        agenda.getOperations().forEach(op -> jsonContext.addData(op.getName() + ".out", "{}"));

        agenda.getOperations().forEach(op ->
        {
            ReferenceReplacementResult result = jsonContext.processReferences(op.getPayload());
            // Can only check for missing. The invalid references check would require knowledge of the output payload format of every handler...
            if(result.getMissingReferences().size() > 0)
            {
                validationIssues.add(String.format(
                    "Invalid references found in operation [%1$s] payload: %2$s",
                    op.getName(),
                    String.join(",", result.getInvalidReferences())));
            }
        });

        if(validationIssues.size() == 0)
            checkForCircularReferences(agenda);
    }

    protected void checkForCircularReferences(Agenda agenda)
    {
        JsonContext jsonContext = new JsonContext();
        JsonReferenceReplacer replacer = jsonContext.getJsonReferenceReplacer();

        Map<String, Set<String>> referenceMap = new HashMap<>();

        agenda.getOperations().forEach(op ->
        {
            ReferenceReplacementResult result = jsonContext.processReferences(op.getPayload());
            referenceMap.put(op.getName(), result.getMissingReferences() == null
                                           ? new HashSet<>()
                                           : result.getMissingReferences().stream().map(ref ->
                                               replacer.getReferenceName(ref).replace(".out", ""))
                                               .collect(Collectors.toSet()));
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
                Set<String> references = new HashSet<>(referenceMap.get(opName));
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

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}