package com.comcast.fission.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.base.progress.reporter.BaseReporterThreadConfig;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;

public class AgendaProgressThreadConfig extends BaseReporterThreadConfig
{
    private ProgressReporter<AgendaProgress> reporter;
    private boolean requireProgressId = true;

    public ProgressReporter<AgendaProgress> getReporter()
    {
        return reporter;
    }

    public AgendaProgressThreadConfig setReporter(ProgressReporter<AgendaProgress> reporter)
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
