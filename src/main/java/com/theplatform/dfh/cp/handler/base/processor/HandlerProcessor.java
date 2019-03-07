package com.theplatform.dfh.cp.handler.base.processor;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
/**
 * Interface for a handler processor.
 *
 * TODO: consider making this an abstract that includes a constructor enforcing the use of a LaunchDataWrapper and BaseOperationContext
 */
public interface HandlerProcessor
{
    void execute();

}
