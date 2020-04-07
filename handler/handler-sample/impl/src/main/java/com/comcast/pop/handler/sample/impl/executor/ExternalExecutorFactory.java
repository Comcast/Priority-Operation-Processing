package com.comcast.pop.handler.sample.impl.executor;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;

public interface ExternalExecutorFactory
{
    BaseExternalExecutor getExternalExecutor(LaunchDataWrapper launchDataWrapper, String[] commandLineArgs);
}