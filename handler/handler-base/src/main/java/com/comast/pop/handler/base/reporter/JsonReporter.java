package com.comast.pop.handler.base.reporter;

import com.comcast.pop.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This reporter converts the objects reported to json for use primarily with the executor.
 * It is mostly intended for the ResidentHandler implementations
 */
public class JsonReporter<T> implements ProgressReporter<T>
{
    private static Logger logger = LoggerFactory.getLogger(JsonReporter.class);
    private JsonHelper jsonHelper;

    private String lastProgress;
    private String payload;
    private String failure;
    private String success;

    public JsonReporter()
    {
        jsonHelper = new JsonHelper();
    }

    @Override
    public void reportProgress(T object)
    {
        lastProgress = jsonHelper.getJSONString(object);
    }

    @Override
    public void reportProgress(T object, Object resultPayload)
    {
        lastProgress = jsonHelper.getJSONString(object);
        payload = jsonHelper.getJSONString(resultPayload);
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public String getLastProgress()
    {
        return lastProgress;
    }

    public void setLastProgress(String lastProgress)
    {
        this.lastProgress = lastProgress;
    }

    public String getFailure()
    {
        return failure;
    }

    public void setFailure(String failure)
    {
        this.failure = failure;
    }

    public String getSuccess()
    {
        return success;
    }

    public void setSuccess(String success)
    {
        this.success = success;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }
}
