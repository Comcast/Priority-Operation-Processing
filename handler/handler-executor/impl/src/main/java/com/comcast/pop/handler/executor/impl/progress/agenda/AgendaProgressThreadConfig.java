package com.comcast.pop.handler.executor.impl.progress.agenda;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comast.pop.handler.base.progress.reporter.BaseReporterThreadConfig;
import com.comast.pop.handler.base.reporter.ProgressReporter;

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
