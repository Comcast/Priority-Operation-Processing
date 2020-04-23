package com.comcast.pop.handler.puller.impl.executor;

import com.comcast.pop.handler.puller.impl.context.PullerContext;

public interface LauncherFactory
{
    BaseLauncher createLauncher(PullerContext pullerContext);
}