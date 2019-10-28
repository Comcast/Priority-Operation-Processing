package com.theplatform.dfh.cp.handler.base.progress.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Reporter threading tests. Hopefully not flaky due to the use of thread sleeps...
 */
public class BaseReporterThreadTest
{
    private static Logger logger = LoggerFactory.getLogger(BaseReporterThreadTest.class);

    private final int MAX_ATTEMPTS_AFTER_SHUTDOWN = 5;
    private final int UPDATE_INTERVAL_MS = 100;
    private final int TEST_SHUTDOWN_SLEEP_MS = 500;
    private TestConfig testConfig = new TestConfig()
        .setUpdateIntervalMilliseconds(UPDATE_INTERVAL_MS)
        .setMaxReportAttemptsAfterShutdown(MAX_ATTEMPTS_AFTER_SHUTDOWN);
    private TestReporter testReporter;

    @BeforeMethod
    public void setup()
    {
        testReporter = new TestReporter(testConfig);
    }

    @Test
    public void testStartAndShutdown() throws Exception
    {
        testReporter.init();
        testReporter.shutdown(false);
        waitOnThread(2);
    }

    @Test
    public void testStartAndAbort() throws Exception
    {
        testReporter.init();
        testReporter.shutdown(true);
        waitOnThread(1);
    }

    @Test
    public void testVerifyReportsCalled() throws Exception
    {
        testReporter.init();
        Thread.sleep(500);
        testReporter.shutdown(false);
        waitOnThread(2);
        logger.info("Reports sent: {}", Integer.toString(testReporter.getReportsSent()));
        Assert.assertTrue(testReporter.getReportsSent() > 0);
    }

    @Test
    public void testReportExceptionIgnored() throws Exception
    {
        testReporter.init();
        Thread.sleep(500);
        testReporter.setReportRuntimeException(new RuntimeException());
        Thread.sleep(500);
        // thread should still be alive and running
        Assert.assertTrue(testReporter.getReporterThread().isAlive());
        testReporter.shutdown(true);
        waitOnThread(1);
    }

    @Test
    public void testReportExceptionAlways() throws Exception
    {
        testReporter.setResetExceptionOnThrow(false);
        testReporter.init();
        Thread.sleep(500);
        testReporter.shutdown(false);
        waitOnThread(2);
        Assert.assertEquals(testReporter.getShutdownReportingAttemptsCount(), testConfig.getMaxReportAttemptsAfterShutdown());
    }

    @Test
    public void testReportExceptionRecover() throws Exception
    {
        testReporter.setResetExceptionOnThrow(false);
        testReporter.init();
        Thread.sleep(500);
        testReporter.shutdown(false);
        // Is there a risk of flakiness if the setResetExceptionOnThrow doesn't apply quick enough?
        testReporter.setResetExceptionOnThrow(true);
        waitOnThread(2);
        // there should be a couple post shutdown attempts
        Assert.assertNotEquals(testReporter.getShutdownReportingAttemptsCount(), 0);
    }

    @Test
    public void testReportExceptionAlwaysAborted() throws Exception
    {
        testReporter.setResetExceptionOnThrow(false);
        testReporter.init();
        Thread.sleep(500);
        testReporter.shutdown(true);
        waitOnThread(2);
        Assert.assertEquals(testReporter.getShutdownReportingAttemptsCount(), 0);
    }

    private void waitOnThread(int intervalsToWait) throws Exception
    {
        for(int count = 0; count < intervalsToWait; count++)
        {
            Thread.sleep(TEST_SHUTDOWN_SLEEP_MS);
            if(!testReporter.getReporterThread().isAlive()) return;
        }
        Assert.fail("The reporter thread should have concluded by now. Forcing deprecated stop call to exit test...");
        testReporter.getReporterThread().stop();
    }

    public static class TestReporter extends BaseReporterThread<TestConfig>
    {
        private boolean progressToReport = true;
        private int reportsSent = 0;
        private RuntimeException reportRuntimeException = null;
        private boolean resetExceptionOnThrow = true;

        public TestReporter(TestConfig testConfig)
        {
            super(testConfig);
        }

        public synchronized void setProgressToReport(boolean progressToReport)
        {
            this.progressToReport = progressToReport;
        }

        @Override
        protected synchronized boolean isThereProgressToReport()
        {
            return progressToReport;
        }

        @Override
        protected void updateProgressItemsToReport()
        {

        }

        public int getReportsSent()
        {
            return reportsSent;
        }

        public synchronized RuntimeException getReportRuntimeException()
        {
            return reportRuntimeException;
        }

        public synchronized void setReportRuntimeException(RuntimeException reportRuntimeException)
        {
            this.reportRuntimeException = reportRuntimeException;
        }

        public synchronized boolean isResetExceptionOnThrow()
        {
            return resetExceptionOnThrow;
        }

        public synchronized void setResetExceptionOnThrow(boolean resetExceptionOnThrow)
        {
            this.resetExceptionOnThrow = resetExceptionOnThrow;
        }

        @Override
        protected void reportProgress()
        {
            // get and throw the exception
            RuntimeException runtimeException = getReportRuntimeException();
            if(runtimeException != null)
            {
                if(isResetExceptionOnThrow()) setReportRuntimeException(null);
                throw runtimeException;
            }

            reportsSent++;
        }

        @Override
        protected String getThreadName()
        {
            return "TestReporterThread";
        }
    }
}
