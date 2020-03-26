package com.theplatform.dfh.cp.handler.sample.impl.action;

import com.theplatform.dfh.cp.handler.sample.api.SampleActions;

import java.util.HashMap;
import java.util.Map;

public class ActionMap
{
    private final static Map<String, BaseAction> ACTION_MAP;
    static
    {
        ACTION_MAP = new HashMap<>();
        ACTION_MAP.put(SampleActions.exception.name(), new ExceptionAction());
        ACTION_MAP.put(SampleActions.log.name(), new LogAction());
    }

    public void addAction(String actionName, BaseAction action)
    {
        ACTION_MAP.put(actionName, action);
    }

    public BaseAction getAction(String actionName)
    {
        return ACTION_MAP.get(actionName);
    }
}
