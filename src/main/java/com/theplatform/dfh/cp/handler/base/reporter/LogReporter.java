package com.theplatform.dfh.cp.handler.base.reporter;

import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogReporter<T> implements ProgressReporter<T>
{
    private static Logger logger = LoggerFactory.getLogger(LogReporter.class);
    private JsonHelper jsonHelper;

    public LogReporter()
    {
        jsonHelper = new JsonHelper();
    }

    @Override
    public void reportProgress(T object)
    {
        logger.info("Progress: " + jsonHelper.getJSONString(object));
    }

    @Override
    public void reportProgress(T object, Object resultPayload)
    {
        logger.info("Progress: " + jsonHelper.getJSONString(object));
        logger.info("Payload: " + jsonHelper.getJSONString(resultPayload));
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
