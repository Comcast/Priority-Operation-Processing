package com.theplatform.dfh.cp.endpoint.agenda.service.reset;

import java.util.HashSet;
import java.util.Set;

public class ProgressResetResult
{
    private Set<String> operationsToReset;
    private Set<String> operationsToDelete;

    public ProgressResetResult()
    {
        this.operationsToReset = new HashSet<>();
        this.operationsToDelete = new HashSet<>();
    }

    public Set<String> getOperationsToReset()
    {
        return operationsToReset;
    }

    public ProgressResetResult setOperationsToReset(Set<String> operationsToReset)
    {
        this.operationsToReset = operationsToReset;
        return this;
    }

    public Set<String> getOperationsToDelete()
    {
        return operationsToDelete;
    }

    public ProgressResetResult setOperationsToDelete(Set<String> operationsToDelete)
    {
        this.operationsToDelete = operationsToDelete;
        return this;
    }
}
