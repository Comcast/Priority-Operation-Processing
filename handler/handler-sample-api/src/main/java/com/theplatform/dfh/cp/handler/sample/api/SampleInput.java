package com.theplatform.dfh.cp.handler.sample.api;

import com.theplatform.dfh.cp.api.params.ParamsMap;

import java.util.List;

public class SampleInput
{
    private List<SampleAction> actions;
    private ParamsMap resultPayload;

    public List<SampleAction> getActions()
    {
        return actions;
    }

    public void setActions(List<SampleAction> actions)
    {
        this.actions = actions;
    }

    public ParamsMap getResultPayload()
    {
        return resultPayload;
    }

    public void setResultPayload(ParamsMap resultPayload)
    {
        this.resultPayload = resultPayload;
    }
}
