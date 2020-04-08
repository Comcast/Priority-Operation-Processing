package com.comcast.pop.handler.executor.impl.processor.runner;

import com.codahale.metrics.Timer;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.processor.OnOperationCompleteListener;
import com.comcast.pop.handler.executor.impl.processor.OperationWrapper;
import com.comcast.pop.handler.executor.impl.processor.runner.event.OperationCompleteLog;
import com.comcast.pop.modules.monitor.metric.MetricLabel;
import com.comcast.pop.modules.monitor.metric.MetricReporter;

public class MonitoringOperationRunner extends OperationRunner
{
    MetricReporter metricReport;

    public MonitoringOperationRunner(MetricReporter metricReport, OperationWrapper operationWrapper, ExecutorContext executorContext,
        OnOperationCompleteListener onOperationCompleteListener)
    {
        super(operationWrapper, executorContext, onOperationCompleteListener);
        setOperationCompleteEvent(new OperationCompleteLog(executorContext));
        this.metricReport = metricReport;
    }

    @Override
    public void run()
    {
        Timer.Context timerContext = metricReport.timerStart("op." +getOperationType() +"." +MetricLabel.duration.name());
        try
        {
            super.run();
        }
        finally
        {
            timerContext.stop();
            if (getOperationWrapper().getSuccess() == Boolean.FALSE)
            {
                metricReport.countInc("op." + getOperationType() + "." + MetricLabel.failed.name());
            }
        }
    }

}
