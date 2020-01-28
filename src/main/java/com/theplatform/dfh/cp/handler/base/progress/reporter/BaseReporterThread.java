package com.theplatform.dfh.cp.handler.base.progress.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base Reporter Thread class providing a basic implementation for reporting on an interval
 * @param <C> The configuration class for the child class to use
 */
public abstract class BaseReporterThread<C extends BaseReporterThreadConfig> implements Runnable
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private C progressReporterConfig;
    // latch for allowing shutdown to disrupt a delay (so we do not waste time!)
    private CountDownLatch reporterThreadDelayLatch = new CountDownLatch(1);
    private Thread reporterThread = null;

    // main flag for indicating whether reporting should continue
    private boolean reporting = true;

    // track the number of attempts to report after shutdown (useful for unit tests/statistics)
    private int shutdownReportingAttemptsCount = 0;

    public BaseReporterThread(C reporterThreadConfig)
    {
        this.progressReporterConfig = reporterThreadConfig;
    }

    /**
     * Setup and thread launch of the background reporter thread
     */
    public void init()
    {
        reporterThread = new Thread(this);
        reporterThread.setName(getThreadName());
        reporterThread.start();
    }

    /**
     * Marks the reporter for shutdown
     * @param forceShutdown flag indicating the thread should be shutdown immediately
     */
    public synchronized void shutdown(boolean forceShutdown)
    {
        // mark the thread as no longer in interval mode
        reporting = false;
        if(forceShutdown)
        {
            // interrupt the thread (forced exit)
            abort();
        }
        reporterThreadDelayLatch.countDown();
    }

    /**
     * Returns if the reporter is still in regular reporting mode
     * @return true if so, false otherwise
     */
    private synchronized boolean isReporting()
    {
        return reporting;
    }

    /**
     * Marks the thread for forced shutdown and interrupts it
     */
    protected void abort()
    {
        reporterThread.interrupt();
    }

    /**
     * Runs the reporting thread in 2 modes: regular interval reporting or shutdown reporting (hopefully finishing reporting any remaining)
     */
    @Override
    public void run()
    {
        try
        {
            // interval reporting
            while (isReporting())
            {
                latchDelay();
                updateProgressItemsToReport();
                reportProgressIgnoreExceptions();
            }

            // after shutdown attempt to update the progress items once
            updateProgressItemsToReport();

            // any outstanding reporting after shutdown requested
            for(int count = 0; count < progressReporterConfig.getMaxReportAttemptsAfterShutdown(); count++)
            {
                if(isThereProgressToReport() && !isThreadInterrupted())
                {
                    shutdownReportingAttemptsCount++;
                    logger.info("Outstanding progress remains to be reported. Attempt: {}/{}",
                        shutdownReportingAttemptsCount,
                        progressReporterConfig.getMaxReportAttemptsAfterShutdown());
                    if(!reportProgressIgnoreExceptions())
                    {
                        delay();
                    }
                }
                else
                {
                    break;
                }
            }
            if(isThereProgressToReport())
            {
                onLostProgress();
                logger.error("Failed to report all progress.");
            }
        }
        catch(InterruptedException e)
        {
            logger.warn("Reporter thread interrupted.", e);
        }
        catch(Throwable t)
        {
            logger.error("Reporter thread threw unexpected exception.", t);
        }
        logger.info("Reporter thread exiting.");
    }

    protected C getProgressReporterConfig()
    {
        return progressReporterConfig;
    }

    /**
     * Waits on the delay latch for the update interval on the configuration. When the latch hits zero this method will return without delay.
     * @throws InterruptedException
     */
    protected void latchDelay() throws InterruptedException
    {
        reporterThreadDelayLatch.await(progressReporterConfig.getUpdateIntervalMilliseconds(), TimeUnit.MILLISECONDS);
    }

    /**
     * Sleeps for the update interval on the configuration
     * @throws InterruptedException
     */
    protected void delay() throws InterruptedException
    {
        logger.warn("Delaying {}ms before attempting to report again", progressReporterConfig.getUpdateIntervalMilliseconds());
        Thread.sleep(progressReporterConfig.getUpdateIntervalMilliseconds());
    }

    public int getShutdownReportingAttemptsCount()
    {
        return shutdownReportingAttemptsCount;
    }

    /**
     * Wrapper for reporting progress. All throwables are logged only.
     * @return true if the progress was reported or the entire thread is interrupted, otherwise false.
     */
    protected boolean reportProgressIgnoreExceptions()
    {
        if(isThreadInterrupted()) return true;

        try
        {
            reportProgress();
            return true;
        }
        catch(Throwable t)
        {
            logger.warn("Ignoring exception from reporting progress.", t);
            return false;
        }
    }

    /**
     * Simple wrapper for Thread.interrupted that logs if it is
     * @return true if thread is, otherwise false
     */
    private boolean isThreadInterrupted()
    {
        if(Thread.interrupted())
        {
            logger.info("Thread is interrupted");
        }
        return Thread.interrupted();
    }

    protected Thread getReporterThread()
    {
        return reporterThread;
    }

    /**
     * Called if progress reporting fails with outstanding progress.
     */
    protected void onLostProgress(){}

    /**
     * After the shutdown method is called is a grace period where outstanding updates may be sent.
     * This method indicates if there is progress to report in the grace period.
     * @return Indicator if there is pending progress to report.
     */
    protected abstract boolean isThereProgressToReport();

    /**
     * Called before each reportProgress call (before shutdown is called)
     */
    protected void updateProgressItemsToReport(){}

    /**
     * Performs the report processing
     */
    protected abstract void reportProgress();

    /**
     * Gets the name to set on the thread
     * @return The name to set on the reporter thread.
     */
    protected abstract String getThreadName();
}
