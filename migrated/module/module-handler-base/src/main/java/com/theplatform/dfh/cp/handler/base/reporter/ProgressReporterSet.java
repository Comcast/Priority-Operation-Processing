package com.theplatform.dfh.cp.handler.base.reporter;

import java.util.HashSet;
import java.util.Set;

public class ProgressReporterSet<T> implements ProgressReporter<T>
{
    private Set<ProgressReporter<T>> reporters;

    public ProgressReporterSet()
    {
        this.reporters = new HashSet<>();
    }

    public void reportProgress(T object)
    {
        reporters.forEach(reporter -> reporter.reportProgress(object));
    }

    public void reportProgress(T object, Object resultPayload)
    {
        reporters.forEach(reporter -> reporter.reportProgress(object, resultPayload));
    }

    public ProgressReporterSet add(ProgressReporter<T> reporter)
    {
        reporters.add(reporter);
        return this;
    }

    public void remove(ProgressReporter reporter)
    {
        reporters.remove(reporter);
    }
}
