package com.theplatform.dfh.cp.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.base.BaseReporterThreadConfig;

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
