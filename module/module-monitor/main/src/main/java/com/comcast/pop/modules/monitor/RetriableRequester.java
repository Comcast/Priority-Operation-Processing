package com.comcast.pop.modules.monitor;

import com.comcast.pop.modules.monitor.alert.AlertConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class RetriableRequester<RETURN>
{
    private static final Logger logger = LoggerFactory.getLogger(RetriableRequester.class);
    private Set<Class<? extends Throwable>> retriableExceptions = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(SocketTimeoutException.class, IOException.class)));
    private Integer retryTimeout = AlertConfigKeys.RETRY_TIMEOUT.getDefaultValue();
    private Integer retryCount = AlertConfigKeys.RETRY_COUNT.getDefaultValue();

    public RetriableRequester(Integer retryTimeout, Integer retryCount, final Set<Class<? extends Throwable>> retriableExceptions)
    {
        this(retryTimeout, retryCount);
        if(retriableExceptions != null)
            this.retriableExceptions = retriableExceptions;
    }

    public RetriableRequester(Integer retryTimeout, Integer retryCount)
    {
        if(retryTimeout != null)
            this.retryTimeout = retryTimeout;
        if(retryCount != null)
            this.retryCount = retryCount;
    }

    public RetriableRequester()
    {
    }

    protected boolean isExceptionRetriable(Throwable exception)
    {
        //descend the caused-by tree
        for (Class<? extends Throwable> exceptionClass : retriableExceptions)
        {
            if (exceptionClass.isAssignableFrom(exception.getClass()) || (exception.getCause() != null && exceptionClass.isAssignableFrom(exception.getCause().getClass())))
            {
                logger.error("Received retriable exception, retrying : " + exception.getMessage(), exception);
                return true;
            }
        }
        logger.error("Received non-retriable exception : " + exception.getMessage(), exception);
        return false;
    }

    public int getRetryCount()
    {
        return this.retryCount;
    }

    public Integer getRetryTimeout()
    {
        return retryTimeout;
    }

    public void setRetryTimeout(Integer retryTimeout)
    {
        this.retryTimeout = retryTimeout;
    }

    /**
     * Retry the method up to the total retries if the exception is retriable.
     * @param callable The method to call
     * @return return value from the method called
     */
    public <RETURN> RETURN retry(Callable<RETURN> callable) throws Exception
    {
        int counter = 0;

        while (counter <= getRetryCount())
        {
            try
            {
                return callable.call();
            }
            catch(Throwable e)
            {
                if(!isExceptionRetriable(e))
                    throw e;

                counter++;
                logger.error("retry {} / {}, {}", counter, getRetryCount(), e);

                try { Thread.sleep(getRetryTimeout()); } catch (InterruptedException ignored) { throw new RuntimeException(e); }
            }
        }
        return null;
    }
}
