package com.theplatform.dfh.cp.endpoint.validation;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.base.validation.DataObjectValidator;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class AgendaProgressValidator extends DataObjectValidator<AgendaProgress, DataObjectRequest<AgendaProgress>>
{
    private List<String> validationIssues;

    @Override
    public void validatePOST(DataObjectRequest<AgendaProgress> request)
    {
        super.validatePOST(request);
        validationIssues = new LinkedList<>();
        AgendaProgress agendaProgress = request.getDataObject();

        validateOperations(agendaProgress.getOperationProgress());
        processValidationIssues(validationIssues);
    }

    protected void validateOperations(OperationProgress[] operationProgresses)
    {
        if(operationProgresses == null)
            return;

        for(OperationProgress op : operationProgresses)
        {
            if(op == null)
                continue;
            if(StringUtils.isBlank(op.getOperation()))
            {
                validationIssues.add("All OperationProgress objects must specify the operation field.");
                break;
            }
        }
    }
}
