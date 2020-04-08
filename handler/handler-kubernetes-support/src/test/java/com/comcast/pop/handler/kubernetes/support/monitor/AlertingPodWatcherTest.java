package com.comcast.pop.handler.kubernetes.support.monitor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.comcast.pop.modules.kube.client.LogLineAccumulator;
import com.comcast.pop.modules.kube.fabric8.client.facade.PodResourceFacade;
import com.comcast.pop.modules.kube.fabric8.client.facade.PodResourceFacadeFactory;
import com.comcast.pop.modules.kube.fabric8.client.follower.ResetableTimeout;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodPhase;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodWatcherImpl;
import com.comcast.pop.modules.monitor.PropertyLoader;
import com.comcast.pop.modules.monitor.alive.AliveCheckListener;
import com.comcast.pop.modules.monitor.alive.LogAliveCheckListener;
import com.comcast.pop.modules.monitor.metric.LoggingMetricReporterFactory;
import com.comcast.pop.modules.monitor.metric.MetricLabel;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.comcast.pop.modules.kube.fabric8.client.follower.PodFollowerImpl.LATCH_TIMEOUT;

public class AlertingPodWatcherTest
{
    private Properties serviceProperties = new Properties();
    private MetricRegistry metricRegistry = new MetricRegistry();
    private MetricReporter metricReporter = new MetricReporter(metricRegistry);
    private Pod pod = Mockito.mock(Pod.class);
    private PodStatus podStatus = Mockito.mock(PodStatus.class);
    private InputStream logWatchInputStream = Mockito.mock(InputStream.class);
    private CountDownLatch scheduledCountdownLatch = new CountDownLatch(100);
    private Meter deleteMetric;
    private Meter failedMetric;
    private PodWatcherImpl podWatcher;
    private AliveCheckListener aliveCheckListener = Mockito.mock(AliveCheckListener.class);
    private AliveCheckPodEventListener aliveCheckPodEventListener;
    @BeforeMethod
    public void setupMetrics()
    {
        serviceProperties.setProperty("banana.tags", "zennos,splunk");
        setupMetrics(serviceProperties, metricReporter);
    }
    @AfterMethod
    public void afterMethod()
    {
        aliveCheckPodEventListener.onClose(null);
    }
    private void setupMetrics(Properties serviceProperties, MetricReporter metricReporter)
    {
        deleteMetric = metricRegistry.meter("pod." + MetricLabel.deleted.name());
        failedMetric = metricRegistry.meter("pod." + MetricLabel.failed.name());
        this.metricReporter = metricReporter;
        MetricPodEventListener metricListener = new MetricPodEventListener(metricReporter);
        aliveCheckPodEventListener = new AliveCheckPodEventListener(serviceProperties, Collections.singletonList(new LogAliveCheckListener()));
        podWatcher = new PodWatcherImpl();
        podWatcher.addEventListeners(Arrays.asList(metricListener, aliveCheckPodEventListener));
        PodResourceFacade podResourceFacade = Mockito.mock(PodResourceFacade.class);
        PodResourceFacadeFactory podResourceFacadeFactory = Mockito.mock(PodResourceFacadeFactory.class);
        Mockito.when(podResourceFacadeFactory.create(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(podResourceFacade);
        podWatcher.setPodResourceFacadeFactory(podResourceFacadeFactory);
        podWatcher.setScheduledLatch(scheduledCountdownLatch);
        podWatcher.setLogLineAccumulator(Mockito.mock(LogLineAccumulator.class));
        podWatcher.setWatch(Mockito.mock(Watch.class));
        podWatcher.setFinishedLatch(Mockito.mock(CountDownLatch.class));
        LogWatch logWatch = Mockito.mock(LogWatch.class);
        Mockito.when(podResourceFacade.watchLog()).thenReturn(logWatch);
        Mockito.when(logWatch.getOutput()).thenReturn(logWatchInputStream);
        Mockito.when(pod.getStatus()).thenReturn(podStatus);
        Mockito.when(podResourceFacade.get()).thenReturn(pod);
    }

    @Test
    public void testDeletedMetric()
    {
        podWatcher.eventReceived(Watcher.Action.DELETED, pod);
        Assert.assertEquals(deleteMetric.getCount(), 1);
    }
    @Test
    public void testFailedMetric()
    {
        Mockito.when(podStatus.getPhase()).thenReturn(PodPhase.FAILED.toString());
        podWatcher.eventReceived(Watcher.Action.MODIFIED, pod);
        Assert.assertEquals(failedMetric.getCount(), 1);
    }

    @Test(enabled = false)
    public void testAlive() throws Exception
    {
        serviceProperties = PropertyLoader.loadResource("../../../../../../test-service.properties");
        LoggingMetricReporterFactory loggerFactory = new LoggingMetricReporterFactory();
        MetricReporter metricReporter = new MetricReporter(metricRegistry);
        metricReporter.register(loggerFactory);
        setupMetrics(serviceProperties, metricReporter);

        Mockito.when(podStatus.getPhase()).thenReturn(PodPhase.RUNNING.toString());
        ResetableTimeout resetableScheduleTimeout = new ResetableTimeout(305000);
        boolean isScheduled = false;
        do
        {
            isScheduled = scheduledCountdownLatch.await(LATCH_TIMEOUT, TimeUnit.MILLISECONDS);
            podWatcher.eventReceived(Watcher.Action.MODIFIED, pod);
            resetableScheduleTimeout.timeout("test pod");
        }
        while (!isScheduled);
        Mockito.verify(aliveCheckListener, Mockito.atLeastOnce()).processAliveCheck(Mockito.anyBoolean());
    }
}
