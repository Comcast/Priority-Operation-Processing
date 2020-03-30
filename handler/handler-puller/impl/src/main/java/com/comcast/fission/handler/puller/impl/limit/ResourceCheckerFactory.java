package com.comcast.fission.handler.puller.impl.limit;

import java.util.List;

public interface ResourceCheckerFactory
{
    List<ResourceChecker> getResourceCheckers();
}
