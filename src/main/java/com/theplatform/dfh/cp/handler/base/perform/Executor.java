package com.theplatform.dfh.cp.handler.base.perform;

/**
 * Interface for performing operations (primarily for the RetryableExecutor)
 * @param <T>
 */
public interface Executor<T>
{
    T execute() throws Exception;
}
