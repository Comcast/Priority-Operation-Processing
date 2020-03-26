package com.theplatform.dfh.cp.modules.monitor.bananas.config;

import com.theplatform.dfh.cp.modules.monitor.alert.AlertConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;

import java.util.Properties;

public class BananasPropertiesFactory
{
    public static ConfigurationProperties from(Properties properties)
    {
        return ConfigurationProperties.from(properties, new BananasConfigKeys(), new AlertConfigKeys(), new AliveCheckConfigKeys());
    }
}
