package com.comcast.pop.handler.kubernetes.support.monitor;

import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comcast.pop.modules.monitor.alert.AlertConfigKeys;
import com.comcast.pop.modules.monitor.alive.AliveCheck;
import com.comcast.pop.modules.monitor.alive.AliveCheckConfigKeys;
import com.comcast.pop.modules.monitor.alive.AliveCheckPoller;
import com.comcast.pop.modules.monitor.config.ConfigurationProperties;
import com.comcast.pop.modules.monitor.metric.MetricAliveCheckListener;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class AliveCheckPollerFactory
{
    private static Logger logger = LoggerFactory.getLogger(AliveCheckPollerFactory.class);

    public static AliveCheckPoller startInstance(AliveCheck aliveCheck, PropertyRetriever propertyRetriever,
        MetricReporter metricReporter)
    {
        try
        {
            if (propertyRetriever != null && propertyRetriever.getPropertyProvider() != null)
            {
                Properties properties = propertyRetriever.getPropertyProvider().getProperties();
                ConfigurationProperties configuationProperties =
                    ConfigurationProperties.from(properties, new AlertConfigKeys(), new AliveCheckConfigKeys());
                MetricAliveCheckListener metricAliveCheckListener = new MetricAliveCheckListener(metricReporter);
                //start alive check poller.
                AliveCheckPoller poller = new AliveCheckPoller(configuationProperties, aliveCheck, Arrays.asList( metricAliveCheckListener));
                poller.start();
                return poller;
            }
        }
        catch (Throwable e)
        {
            //don't crash our application if we can't send alerts.
            logger.error("Error occurred trying to initialize alive check. Alive check is now disabled.", e);
        }
        return null;
    }
}
