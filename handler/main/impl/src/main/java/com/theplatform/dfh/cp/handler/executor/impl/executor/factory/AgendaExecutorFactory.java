package com.theplatform.dfh.cp.handler.executor.impl.executor.factory;

import com.theplatform.dfh.cp.handler.base.perform.Executor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

import java.util.List;

public interface AgendaExecutorFactory
{
    Executor<List<String>> getMediaInfoExecutor(String filePath, LaunchDataWrapper launchDataWrapper);
}