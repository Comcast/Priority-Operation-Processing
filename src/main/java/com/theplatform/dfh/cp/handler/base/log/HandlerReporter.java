package com.theplatform.dfh.cp.handler.base.log;

import java.util.Map;

public interface HandlerReporter
{
    void reportMetadata(Map<String,String> metadata);
}
