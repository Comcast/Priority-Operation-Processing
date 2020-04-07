package com.comcast.pop.handler.executor.impl.processor.parallel;

import com.codahale.metrics.Timer;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricLabel;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;

import java.util.Collection;

public class MonitoringOperationConductor extends OperationConductor
{
    private MetricReporter metricReport;
    public MonitoringOperationConductor(MetricReporter metricReport, Collection<Operation> operations, ExecutorContext executorContext)
    {
        super(operations, executorContext);
        this.metricReport = metricReport;
    }

    @Override
    public void run()
    {
        Timer.Context timerContext = metricReport.timerStart(MetricLabel.duration);
        try
        {
            super.run();
        }
        finally
        {
            timerContext.stop();
            metricReport.report();
        }
    }
}
