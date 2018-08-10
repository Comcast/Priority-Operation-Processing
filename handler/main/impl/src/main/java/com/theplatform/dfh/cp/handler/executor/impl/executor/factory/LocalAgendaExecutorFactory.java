package com.theplatform.dfh.cp.handler.executor.impl.executor.factory;

import com.theplatform.dfh.cp.handler.executor.impl.executor.LocalAgendaExecutor;
import com.theplatform.dfh.cp.handler.base.perform.Executor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

import java.util.List;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through a docker container).
 * This may only apply to functional tests.
 */
public class LocalAgendaExecutorFactory implements AgendaExecutorFactory
{
    @Override
    public Executor<List<String>> getMediaInfoExecutor(String filePath, LaunchDataWrapper launchDataWrapper)
    {
        return new LocalAgendaExecutor(filePath);
    }
}