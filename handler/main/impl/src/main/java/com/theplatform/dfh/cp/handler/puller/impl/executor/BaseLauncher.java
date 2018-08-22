package com.theplatform.dfh.cp.handler.puller.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;

import java.util.UUID;

public interface BaseLauncher
{
    public abstract void execute(String payload);
}