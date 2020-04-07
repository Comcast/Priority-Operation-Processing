package com.comcast.pop.handler.puller.impl.healthcheck;

import com.comast.pop.handler.base.field.retriever.properties.PropertyProvider;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comcast.pop.handler.kubernetes.support.monitor.AliveCheckPollerFactory;
import com.comcast.pop.handler.kubernetes.support.monitor.MetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.PropertyLoader;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricFilterBuilder;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Properties;

public class AliveCheckPollerTest implements AliveCheck
{
    private Boolean[] isAliveState = {  true, false, false, false, true };
    private boolean keepChecking = true;
    private int stateIndex = 0;
    private AliveCheckPoller poller;

    @Override
    public boolean isAlive()
    {
        if(stateIndex == isAliveState.length - 1)
        {
            poller.stop();
            keepChecking = false;
        }
        return isAliveState[stateIndex ++];
    }
    @Test(enabled = false)
    public void testAlive()
    {
        Properties serviceProperties = PropertyLoader.load("../package/local/config/external.properties");
        PropertyRetriever propertyRetriever = Mockito.mock(PropertyRetriever.class);
        PropertyProvider propertyProvider = Mockito.mock(PropertyProvider.class);
        Mockito.when(propertyRetriever.getPropertyProvider()).thenReturn(propertyProvider);
        Mockito.when(propertyProvider.getProperties()).thenReturn(serviceProperties);

        MetricFilterBuilder.MetricFilter metricFilter = new MetricFilterBuilder().filterCountZero().filterTimer().build();
        MetricReporter metricReporter = MetricReporterFactory.getInstance(propertyRetriever, metricFilter);
        AliveCheckPoller poller = AliveCheckPollerFactory.startInstance(this, propertyRetriever, metricReporter);

        while(keepChecking)
        {

        }
    }
}
