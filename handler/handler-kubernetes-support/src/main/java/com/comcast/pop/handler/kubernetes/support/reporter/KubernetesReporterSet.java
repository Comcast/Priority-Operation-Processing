package com.comcast.pop.handler.kubernetes.support.reporter;

import com.comast.pop.handler.base.reporter.LogReporter;
import com.comast.pop.handler.base.reporter.ProgressReporterSet;

public class KubernetesReporterSet<T> extends ProgressReporterSet<T>
{
    public KubernetesReporterSet()
    {
        add(new LogReporter<>());
        add(new KubernetesReporter<>());
    }
}
