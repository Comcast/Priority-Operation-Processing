package com.theplatform.dfh.cp.endpoint.agenda;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.ReferenceReplacementResult;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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