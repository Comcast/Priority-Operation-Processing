package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.MonitoringOperationRunnerFactory;
import com.theplatform.dfh.cp.handler.kubernetes.monitor.MetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;

import java.util.Collection;

public class MonitoringOperationConductorFactory extends OperationConductorFactory
{
    private MetricReporter metricReport;
    public MonitoringOperationConductorFactory(ExecutorContext executorContext)
    {
        //register a logging reporter
        this.metricReport = MetricReporterFactory.getInstance(executorContext.getLaunchDataWrapper().getPropertyRetriever());
    }

    @Override
    public OperationConductor createOperationConductor(Collection<Operation> operations, ExecutorContext executorContext)
    {
        MonitoringOperationConductor conductor = new MonitoringOperationConductor(metricReport, operations, executorContext);
        conductor.setOperationRunnerFactory(new MonitoringOperationRunnerFactory(metricReport));
        return conductor;
    }
}
