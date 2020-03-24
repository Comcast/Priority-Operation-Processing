package com.theplatform.dfh.cp.handler.base.perform;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

public class RetryableExecutorTest
{
    private RetryableExecutor<TestDataObject> retryableExecutor;
    private TestExecutor testExecutor;

    @BeforeMethod
    public void setup()
    {
        testExecutor = new TestExecutor();

    }

    @DataProvider
    public Object[][] retriesTestProvider()
    {
        return new Object[][]
            {
                {null, 0, 0, 1},
                {new RuntimeException(), 1, 1, 2},
                {new RuntimeException(), 1, 0, 1},
                {new RuntimeException(), 5, 10, 6},
                {new RuntimeException(), 10, 1, 2}
            };
    }

    @Test(dataProvider = "retriesTestProvider")
    public void testRetries(Exception exceptionToThrow, final int exceptionCount, final int maxRetries, final int expectedAttemptCount)
    {
        setupTest(exceptionToThrow, exceptionCount, maxRetries, null, null);
        try
        {
            retryableExecutor.execute();
            if(exceptionCount > maxRetries)
            {
                Assert.fail("Expected " + exceptionToThrow.getClass() + " exception.");
            }
        }
        catch(Exception ex)
        {
            Assert.assertEquals(ex, exceptionToThrow);
        }
        Assert.assertEquals(testExecutor.getAttemptCount(), expectedAttemptCount);
    }

    @DataProvider
    public Object[][] retriesExceptionRuleTestProvider()
    {
        // the test assumes 1 retry then success
        return new Object[][]
            {
                {new RuntimeException(), RuntimeException.class, TestException.class},
                {new RuntimeException(), TestException.class, RuntimeException.class}
            };
    }

    @Test(dataProvider = "retriesExceptionRuleTestProvider")
    public void testExceptionRuleRetries(Exception exceptionToThrow, Class retryableExceptionClass, Class nonRetryableExceptionClass)
    {
        final int MAX_RETRIES = 1;
        int expectedAttempts = exceptionToThrow.getClass() == nonRetryableExceptionClass ? 1 : 2;
        setupTest(exceptionToThrow, 1, MAX_RETRIES, Collections.singletonList(retryableExceptionClass), Collections.singletonList(nonRetryableExceptionClass));
        try
        {
            retryableExecutor.execute();
        }
        catch(Exception ex)
        {
            Assert.assertEquals(ex, exceptionToThrow);
        }
        Assert.assertEquals(testExecutor.getAttemptCount(), expectedAttempts);
    }


    private void setupTest(Exception exceptionToThrow, final int exceptionCount, final int maxRetries,
        List<Class<? extends Throwable>> retryableExceptions,
        List<Class<? extends Throwable>> nonRetryableExceptions)
    {
        testExecutor.setupExceptionThrow(exceptionToThrow, exceptionCount);
        retryableExecutor = new RetryableExecutor<>(testExecutor, maxRetries, retryableExceptions, nonRetryableExceptions);
    }

    // Could use mocks, but that's just going to be ugly

    class TestException extends Exception
    {

    }

    class TestDataObject
    {
        private String theField;

        public TestDataObject(String theField)
        {
            this.theField = theField;
        }

        public String getTheField()
        {
            return theField;
        }

        public void setTheField(String theField)
        {
            this.theField = theField;
        }
    }

    class TestExecutor implements Executor<TestDataObject>
    {
        private TestDataObject testDataObject;
        private int attemptCount = 0;
        private Exception exceptionToThrow;
        private int exceptionsToThrow = 0;

        public TestExecutor()
        {

        }

        public TestExecutor(String fieldValue)
        {
            testDataObject = new TestDataObject(fieldValue);
        }

        @Override
        public TestDataObject execute() throws Exception
        {
            attemptCount++;
            if(exceptionsToThrow > 0 && exceptionToThrow != null)
            {
                exceptionsToThrow--;
                throw exceptionToThrow;
            }
            return testDataObject;
        }

        public int getAttemptCount()
        {
            return attemptCount;
        }

        public void setupExceptionThrow(Exception exceptionToThrow, int exceptionsToThrow)
        {
            this.exceptionToThrow = exceptionToThrow;
            this.exceptionsToThrow = exceptionsToThrow;
        }
    }
}
