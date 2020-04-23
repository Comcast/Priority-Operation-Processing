package com.comcast.pop.handler.executor.impl.processor;

/**
 * Basic callback interface. TODO: If this is used extensively it should become an observable... (something with a collection)
 */
public interface OnOperationCompleteListener
{
    void onComplete(OperationWrapper operationWrapper);
}
