package com.theplatform.dfh.cp.handler.kubernetes.support.reporter;

import com.theplatform.dfh.cp.handler.base.reporter.LogReporter;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporterSet;

public class KubernetesReporterSet<T> extends ProgressReporterSet<T>
{
    public KubernetesReporterSet()
    {
        add(new LogReporter<>());
        add(new KubernetesReporter<>());
    }
}
