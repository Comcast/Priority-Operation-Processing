package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.kubernetes.monitor.GraphiteConfiguration;
import com.theplatform.dfh.cp.modules.monitor.graphite.GraphiteMetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.LoggingMetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReport;

import java.util.Collection;

public class MonitoringOperationConductorFactory extends OperationConductorFactory
{
    private MetricReport metricReport;
    public MonitoringOperationConductorFactory(ExecutorContext executorContext)
    {
        //register a logging reporter
        MetricReport metricReport = new MetricReport().register(new LoggingMetricReporterFactory());

        //register a graphite reporter if configured
        GraphiteConfiguration graphiteConfiguration = new GraphiteConfiguration(executorContext.getLaunchDataWrapper().getPropertyRetriever());
        if(graphiteConfiguration.isEnabled())
        {
            GraphiteMetricReporterFactory reporterFactory = new GraphiteMetricReporterFactory(graphiteConfiguration);
            metricReport.register(reporterFactory);
        }
        this.metricReport = metricReport;
    }

    @Override
    public OperationConductor createOperationConductor(Collection<Operation> operations, ExecutorContext executorContext)
    {
        return new MonitoringOperationConductor(metricReport, operations, executorContext);
    }
}
