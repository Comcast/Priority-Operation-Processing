package com.theplatform.dfh.cp.agenda.reclaim.config;

import java.util.LinkedList;
import java.util.List;

public class ReclaimerConfig
{
    private int maximumExecutionSeconds = 60;
    private boolean logReclaimOnly = false;

    public int getMaximumExecutionSeconds()
    {
        return maximumExecutionSeconds;
    }

    public ReclaimerConfig setMaximumExecutionSeconds(int maximumExecutionSeconds)
    {
        this.maximumExecutionSeconds = maximumExecutionSeconds;
        return this;
    }

    public boolean getLogReclaimOnly()
    {
        return logReclaimOnly;
    }

    public ReclaimerConfig setLogReclaimOnly(boolean logReclaimOnly)
    {
        this.logReclaimOnly = logReclaimOnly;
        return this;
    }

    public String validate()
    {
        List<String> validationIssues = new LinkedList<>();

        if(maximumExecutionSeconds < 0)
            validationIssues.add("maximumExecutionSeconds must be non-negative");

        return validationIssues.size() == 0
               ? null
               : String.join(",", validationIssues);

    }
}
