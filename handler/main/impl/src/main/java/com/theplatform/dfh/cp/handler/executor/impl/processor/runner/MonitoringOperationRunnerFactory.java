package com.theplatform.dfh.cp.handler.executor.impl.processor.runner;

import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReport;

public class MonitoringOperationRunnerFactory extends OperationRunnerFactory
{
    MetricReport metricReport;

    public MonitoringOperationRunnerFactory(MetricReport metricReport)
    {
        this.metricReport = metricReport;
    }

    @Override
    public OperationRunner createOperationRunner(OperationWrapper operationWrapper, ExecutorContext executorContext, OnOperationCompleteListener onOperationCompleteListener)
    {
        return new MonitoringOperationRunner(metricReport, operationWrapper, executorContext, onOperationCompleteListener);
    }
}
