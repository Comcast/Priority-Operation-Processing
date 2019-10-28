package com.theplatform.dfh.cp.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.handler.base.progress.reporter.BaseReporterThreadConfig;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;

public class AgendaProgressThreadConfig extends BaseReporterThreadConfig
{
    private ProgressReporter reporter;
    private boolean requireProgressId = true;

    public ProgressReporter getReporter()
    {
        return reporter;
    }

    public AgendaProgressThreadConfig setReporter(ProgressReporter reporter)
    {
        this.reporter = reporter;
        return this;
    }

    public boolean getRequireProgressId()
    {
        return requireProgressId;
    }

    public AgendaProgressThreadConfig setRequireProgressId(boolean requireProgressId)
    {
        this.requireProgressId = requireProgressId;
        return this;
    }
}
