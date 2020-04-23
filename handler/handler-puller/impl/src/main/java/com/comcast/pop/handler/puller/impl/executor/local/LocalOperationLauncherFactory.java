package com.comcast.pop.handler.puller.impl.executor.local;

import com.comcast.pop.handler.puller.impl.context.PullerContext;
import com.comcast.pop.handler.puller.impl.executor.BaseLauncher;
import com.comcast.pop.handler.puller.impl.executor.LauncherFactory;

/**
 * Factory for producing launchers.
 * This may only apply to functional tests.
 */
public class LocalOperationLauncherFactory implements LauncherFactory
{
    @Override
    public BaseLauncher createLauncher(PullerContext handlerContext)
    {
        return new LocalOperationLauncher();
    }
}