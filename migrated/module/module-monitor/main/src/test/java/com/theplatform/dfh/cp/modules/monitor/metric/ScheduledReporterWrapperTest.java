package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.ScheduledReporter;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigKey;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.SortedMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ScheduledReporterWrapperTest
{
    private final ConfigKey<Boolean> ENABLED_KEY = new ConfigKey<>("enabled", true, Boolean.class);
    private ScheduledReporter mockReporter;
    private ScheduledReporterWrapper<ScheduledReporter> wrapper;
    private ConfigurationProperties mockProperties;

    @BeforeMethod
    public void setup()
    {
        mockProperties = mock(ConfigurationProperties.class);
        mockReporter = mock(ScheduledReporter.class);
        wrapper = new ScheduledReporterWrapper<>(mockProperties, mockReporter, ENABLED_KEY);
    }

    @Test
    public void testReportException()
    {
        doReturn(true).when(mockProperties).get(ENABLED_KEY);
        doThrow(new RuntimeException()).when(mockReporter).report();
        doThrow(new RuntimeException()).when(mockReporter).report(
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class));

        wrapper.report();
        verify(mockReporter, times(1)).report();
        wrapper.report(null, null, null, null, null);
        verify(mockReporter, times(1)).report(
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class)
        );
    }

    @Test
    public void testDisabled()
    {
        doReturn(false).when(mockProperties).get(ENABLED_KEY);
        wrapper.report();
        verify(mockReporter, times(0)).report();
        wrapper.report(null, null, null, null, null);
        verify(mockReporter, times(0)).report(
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class),
            any(SortedMap.class)
        );
    }
}
