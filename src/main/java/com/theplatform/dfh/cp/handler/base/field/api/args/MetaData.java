package com.theplatform.dfh.cp.handler.base.field.api.args;

import java.util.Map;

public interface MetaData<T>
{
    Map<String, T> getMetadata();
}
