package com.comast.pop.handler.base.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HandlerReporterImpl implements HandlerReporter
{
    private static Logger logger = LoggerFactory.getLogger(HandlerReporterImpl.class);
    public static final String OPERATION_METADATA_TEMPLATE_PREFIX = "Operation metadata - ";
    @Override
    public void reportMetadata(Map<String, String> metadata)
    {
        StringBuilder b = new StringBuilder();
        for(String key: metadata.keySet())
        {
            b.append(key);
            b.append(" : ");
            b.append(metadata.get(key));
            b.append(" ");
        }
        logger.info(OPERATION_METADATA_TEMPLATE_PREFIX + b.toString());
    }
}
