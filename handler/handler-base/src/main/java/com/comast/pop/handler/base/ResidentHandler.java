package com.comast.pop.handler.base;

/**
 * Represents a handler that is executed directly as a class (no external launch to execute)
 */
public interface ResidentHandler
{
    String execute(ResidentHandlerParams residentHandlerParams);
}
