package com.comast.pop.handler.base.log;

import java.util.Map;

public interface HandlerReporter
{
    void reportMetadata(Map<String,String> metadata);
}
