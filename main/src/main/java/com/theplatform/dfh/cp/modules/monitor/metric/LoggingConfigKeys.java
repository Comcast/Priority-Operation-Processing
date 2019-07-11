package com.theplatform.dfh.cp.modules.monitor.metric;

import com.theplatform.dfh.cp.modules.monitor.config.ConfigKey;

public class LoggingConfigKeys
{
    public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("logging.metric.enabled", true, Boolean.class);
    public static final ConfigKey<Integer> REPORT_FREQUENCY = new ConfigKey<>("logging.metric.report.frequency", 120000, Integer.class);
}
