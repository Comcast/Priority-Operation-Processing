package com.cts.fission.scheduling.monitor;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricLabel;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight.ByInsightId;
import com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.field.CountField;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

public class QueueMetricMonitor
{
    private final static Logger logger = LoggerFactory.getLogger(QueueMetricMonitor.class);
    private final static String METRIC_WAITING = "waiting";

    private ObjectClient<Insight> insightClient;

    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private MetricReporter metricReporter;

    public QueueMetricMonitor(
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectClient<Insight> insightClient,
        MetricReporter metricReporter)
    {
        this.readyAgendaPersister = readyAgendaPersister;
        this.insightClient = insightClient;
        this.metricReporter = metricReporter == null ? new MetricReporter() : metricReporter;
    }

    public void monitor(String resourcePoolId) throws Throwable
    {
        DataObjectResponse<Insight> insightObjectFeed =
            insightClient.getObjects(Collections.singletonList(new ByResourcePoolId(resourcePoolId)));
        if(insightObjectFeed.isError())
        {
            logger.error("Error getting any insights by resourcePoolId: {}", resourcePoolId);
            reportFailed("resourcePool");
            return;
        }
        if(insightObjectFeed.getAll() == null || insightObjectFeed.getAll().size() == 0)
        {
            logger.info("No insights found for ResourcePool: {}", resourcePoolId);
            return;
        }

        for(Insight insight : insightObjectFeed.getAll())
        {
            try
            {
                DataObjectFeed feed = readyAgendaPersister.retrieve(Arrays.asList(new ByInsightId(insight.getId()), new Query(new CountField(), true)));
                Integer waitingAgendaCount = feed.getCount();
                reportWaiting(insight.getId(), waitingAgendaCount);

                logger.info("Insight: {} Waiting: {}", insight.getId(), waitingAgendaCount);
            }
            catch(Exception e)
            {
                logger.error("Failed to process ResourcePool: {} Insight: {}", resourcePoolId, insight.getId(), e);
                reportFailed(insight.getId());
            }
        }
        logger.info("Processed ResourcePool: {}", resourcePoolId);
    }

    public void setMetricReporter(MetricReporter metricReporter)
    {
        this.metricReporter = metricReporter;
    }

    private void reportFailed(String insight)
    {
        metricReporter.getCounter(MetricLabel.failed +"." +insight).inc();
        metricReporter.report();
    }
    private void reportWaiting(String insight, Integer count)
    {
        if(count == null) return;

        for(int countIndex = 0; countIndex < count; countIndex ++)
            metricReporter.getCounter(METRIC_WAITING +"." +insight).inc();
        metricReporter.report();
    }
}
