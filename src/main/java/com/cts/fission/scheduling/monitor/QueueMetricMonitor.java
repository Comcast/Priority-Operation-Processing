package com.cts.fission.scheduling.monitor;

import com.codahale.metrics.Counter;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight.ByInsightId;
import com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

/**
 * Looks at the queue size for the resource pool and sends a waiting count metric if it's more than zero.
 */
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

    /**
     * Read all insights for a resource pool, check their wait queue and report the waiting count.
     * @param resourcePoolId Resource pool to monitor queue for metrics
     * @throws Throwable Something bad happened while reporting.
     */
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
            // simple space replace for
            String insightTitle = getReportSafeInsightTitle(insight);
            try
            {
                DataObjectFeed feed = readyAgendaPersister.retrieve(Arrays.asList(new ByInsightId(insight.getId())));
                Integer waitingAgendaCount = feed.getAll() != null ? feed.getAll().size() : 0;
                if(waitingAgendaCount != null && waitingAgendaCount > 0)
                    reportWaiting(insightTitle, waitingAgendaCount);

                logger.info("Insight: {} Waiting: {}", insightTitle, waitingAgendaCount);
            }
            catch(Exception e)
            {
                logger.error("Failed to process ResourcePool: {} Insight: {}", resourcePoolId, insightTitle, e);
                reportFailed(insightTitle);
            }
        }
        logger.info("Processed ResourcePool: {}", resourcePoolId);
    }

    protected static String getReportSafeInsightTitle(Insight insight)
    {
        return insight.getTitle() == null
            ? null
            : insight.getTitle().replaceAll("\\s+" ,"_");
   }

    public void setMetricReporter(MetricReporter metricReporter)
    {
        this.metricReporter = metricReporter;
    }

    private void reportFailed(String insight)
    {
        final String waitMetric = METRIC_WAITING + "." + insight;
        try
        {
            metricReporter.countInc(waitMetric);
            metricReporter.report();
        }
        finally
        {
            //after we report, I wish we could null it out. We can't wait for the timed reporting since we don't
            //know if/when we'll get a new instance of our lambda running.
            metricReporter.countDec(waitMetric);
        }
    }
    private void reportWaiting(String insight, Integer count)
    {
        if (count == null)
            return;
        Counter waitingCounter = metricReporter.getMetricRegistry().counter(METRIC_WAITING + "." + insight);
        try
        {
            for (int countIndex = 0; countIndex < count; countIndex++)
                waitingCounter.inc();
            metricReporter.report();
        }
        finally
        {
            //after we report, I wish we could null it out. We can't wait for the timed reporting since we don't
            //know if/when we'll get a new instance of our lambda running.
            for (int countIndex = 0; countIndex < count; countIndex++)
                waitingCounter.dec();
        }
    }

}
