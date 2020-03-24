package com.theplatform.dfh.cp.handler.kubernetes.support.monitor;

import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
import com.theplatform.dfh.cp.modules.monitor.bananas.BananasAliveCheckListener;
import com.theplatform.dfh.cp.modules.monitor.bananas.config.BananasConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricAliveCheckListener;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
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
                    ConfigurationProperties.from(properties, new BananasConfigKeys(), new AlertConfigKeys(), new AliveCheckConfigKeys());
                BananasAliveCheckListener bananasAliveCheck = new BananasAliveCheckListener(configuationProperties);
                MetricAliveCheckListener metricAliveCheckListener = new MetricAliveCheckListener(metricReporter);
                //start alive check poller.
                AliveCheckPoller poller = new AliveCheckPoller(configuationProperties, aliveCheck, Arrays.asList(bananasAliveCheck, metricAliveCheckListener));
                poller.start();
                return poller;
            }
        }
        catch (Throwable e)
        {
            //don't crash our application if we can't send alerts.
            logger.error("Error occurred trying to initialize alive check via Bananas. Alive check is now disabled.", e);
        }
        return null;
    }
}
