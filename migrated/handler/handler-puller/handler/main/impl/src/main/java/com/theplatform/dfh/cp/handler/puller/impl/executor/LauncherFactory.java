package com.theplatform.dfh.cp.handler.puller.impl.executor;

import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;

public interface LauncherFactory
{
    BaseLauncher createLauncher(PullerContext pullerContext);
}