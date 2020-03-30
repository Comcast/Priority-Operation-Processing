package com.comcast.fission.handler.puller.impl.executor;

import com.comcast.fission.handler.puller.impl.context.PullerContext;

public interface LauncherFactory
{
    BaseLauncher createLauncher(PullerContext pullerContext);
}