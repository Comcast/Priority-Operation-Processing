package com.theplatform.dfh.cp.handler.base.perform;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performs operations with a retry policy around exception handling
 * @param <T> The type of object returned by the operation
 */
public class RetryableExecutor<T> implements Executor<T>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Executor<T> executor;
    private final int maxRetries;
    private int currentRetry = 0;
    private List<Class<? extends Throwable>> nonRetryableExceptions;
    private List<Class<? extends Throwable>> retryableExceptions;

    /**
     * Constructor that defaults to retrying on RuntimExceptions
     * @param executor The operation performer to call
     * @param maxRetries The maximum number of retries to allow (0+)
     */
    public RetryableExecutor(
        Executor<T> executor,
        int maxRetries)
    {
        this(executor, maxRetries, Collections.singletonList(RuntimeException.class));
    }
    /**
     * Constructor
     * @param executor The operation performer to call
     * @param maxRetries The maximum number of retries to allow (0+)
     * @param retryableExceptions The retryable Exceptions
     */
    public RetryableExecutor(
        Executor<T> executor,
        int maxRetries,
        List<Class<? extends Throwable>> retryableExceptions)
    {
        this.executor = executor;
        this.maxRetries = maxRetries;
        this.retryableExceptions = filterOutNulls(retryableExceptions);
    }

    /**
     * Constructor
     * @param operationPerformer The operation performer to call
     * @param maxRetries The maximum number of retries to allow (0+)
     * @param retryableExceptions The retryable Exceptions
     * @param nonRetryableExceptions The non-retryable Exceptions
     */
    public RetryableExecutor(
        Executor<T> operationPerformer,
        int maxRetries,
        List<Class<? extends Throwable>> retryableExceptions,
        List<Class<? extends Throwable>> nonRetryableExceptions)
    {
        this(operationPerformer, maxRetries, retryableExceptions);
        this.nonRetryableExceptions = filterOutNulls(nonRetryableExceptions);
    }

    private static List<Class<? extends Throwable>> filterOutNulls(List<Class<? extends Throwable>> list)
    {
        return Optional.ofNullable(list).orElse(new ArrayList<>()).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Performs the operation with a retry policy
     * @return The result of the operation.
     */
    @Override
    public T execute()
    {
        logger.debug("Initializing retry");
        RetryPolicy<Object> policy = new RetryPolicy<>();

        if (retryableExceptions != null && retryableExceptions.size() > 0)
        {
            policy.handle(retryableExceptions);
        }

        if (nonRetryableExceptions != null && nonRetryableExceptions.size() > 0)
        {
            policy.abortOn(nonRetryableExceptions);
        }

        policy = policy.withMaxRetries(maxRetries);

        return Failsafe
            .with(policy)
            .onFailure(executionCompletedEvent ->
            {
                currentRetry++;
                logger.error("Retrying Operation {} :", currentRetry, executionCompletedEvent.getFailure());
            })
            .get((() ->
            {
                logger.debug("Running Operation - Attempt: {}", currentRetry);
                return executor.execute();
            }));
    }

}