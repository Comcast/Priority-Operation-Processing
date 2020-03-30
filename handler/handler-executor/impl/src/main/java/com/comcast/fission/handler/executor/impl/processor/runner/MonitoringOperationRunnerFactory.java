package com.comcast.fission.handler.executor.impl.processor.runner;

import com.comcast.fission.handler.executor.impl.context.ExecutorContext;
import com.comcast.fission.handler.executor.impl.processor.OnOperationCompleteListener;
import com.comcast.fission.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;

public class MonitoringOperationRunnerFactory extends OperationRunnerFactory
{
    MetricReporter metricReport;

    public MonitoringOperationRunnerFactory(MetricReporter metricReport)
    {
        this.metricReport = metricReport;
    }

    @Override
    public OperationRunner createOperationRunner(OperationWrapper operationWrapper, ExecutorContext executorContext, OnOperationCompleteListener onOperationCompleteListener)
    {
        return new MonitoringOperationRunner(metricReport, operationWrapper, executorContext, onOperationCompleteListener);
    }

    @Override
    public void shutdown()
    {
        if(metricReport != null)
            metricReport.close();
    }
}
