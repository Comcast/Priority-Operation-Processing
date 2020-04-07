package com.comcast.pop.handler.puller.impl.dropwizard;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.logging.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a custom LoggingFactory intended for use with DropWizard to allow it to use the logback.xml when executing.
 *
 * References:
 * https://github.com/dropwizard/dropwizard/issues/1567
 * Taken from https://gist.github.com/fedotxxl/0b3cc5e5e4eaeffdcde1f9834796edc6
 */
public class LogbackAutoConfigLoggingFactory implements LoggingFactory {

    private static Logger logger = LoggerFactory.getLogger(LogbackAutoConfigLoggingFactory.class);

    @JsonIgnore
    private LoggerContext loggerContext;
    @JsonIgnore
    private final ContextInitializer contextInitializer;

    public LogbackAutoConfigLoggingFactory() {
        this.loggerContext = LoggingUtil.getLoggerContext();
        this.contextInitializer = new ContextInitializer(loggerContext);
    }

    @Override
    public void configure(MetricRegistry metricRegistry, String name) {
        try {
            loggerContext.reset();
            contextInitializer.autoConfig();
        } catch (JoranException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset()
    {
        loggerContext.reset();
    }

    @Override
    public void stop() {
        loggerContext.stop();
    }
}