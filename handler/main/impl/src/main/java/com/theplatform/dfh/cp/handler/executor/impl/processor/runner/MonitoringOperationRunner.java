package com.theplatform.dfh.cp.handler.executor.impl.processor.runner;

import com.codahale.metrics.Timer;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricLabel;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReport;

public class MonitoringOperationRunner extends OperationRunner
{
    MetricReport metricReport;

    public MonitoringOperationRunner(MetricReport metricReport, OperationWrapper operationWrapper, ExecutorContext executorContext,
        OnOperationCompleteListener onOperationCompleteListener)
    {
        super(operationWrapper, executorContext, onOperationCompleteListener);
        this.metricReport = metricReport;
    }

    @Override
    public void run()
    {
        Timer.Context timerContext = metricReport.getMetricRegistry().timer("op-" +getOperationType() +"." +MetricLabel.duration.name()).time();
        try
        {
            super.run();
        }
        finally
        {
            timerContext.stop();
            if(getOperationWrapper().getSuccess() == Boolean.FALSE)
                metricReport.getMetricRegistry().meter("op-" +getOperationType() +"." +MetricLabel.failed.name());
        }
    }

}
