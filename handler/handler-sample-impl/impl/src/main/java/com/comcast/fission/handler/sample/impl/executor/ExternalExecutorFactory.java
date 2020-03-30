package com.comcast.fission.handler.sample.impl.executor;

import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;

public interface ExternalExecutorFactory
{
    BaseExternalExecutor getExternalExecutor(LaunchDataWrapper launchDataWrapper, String[] commandLineArgs);
}