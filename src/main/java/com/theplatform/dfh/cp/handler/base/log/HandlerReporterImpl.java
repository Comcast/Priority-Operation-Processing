package com.theplatform.dfh.cp.handler.base.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HandlerReporterImpl implements HandlerReporter
{
    private static Logger logger = LoggerFactory.getLogger(HandlerReporterImpl.class);
    private static final String KEY_VALUE_TEMPLATE = "Operation metadata - %s : %s";
    @Override
    public void reportMetadata(Map<String, String> metadata)
    {
        for(String key: metadata.keySet())
        {
            logger.info(String.format(KEY_VALUE_TEMPLATE, key, metadata.get(key)));
        }
    }
}
