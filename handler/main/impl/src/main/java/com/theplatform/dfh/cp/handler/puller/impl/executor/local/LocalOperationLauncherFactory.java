package com.theplatform.dfh.cp.handler.puller.impl.executor.local;

import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;

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