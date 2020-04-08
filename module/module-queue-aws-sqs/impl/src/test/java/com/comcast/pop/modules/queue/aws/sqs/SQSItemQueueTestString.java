package com.comcast.pop.modules.queue.aws.sqs;

import org.testng.annotations.DataProvider;

public class SQSItemQueueTestString extends SQSItemQueueTest<String>
{
    @Override
    public String getTestObject()
    {
        return "";
    }

    @Override
    public Class getTestObjectClass()
    {
        return String.class;
    }

    @Override
    @DataProvider
    public Object[][] itemsDataProvider()
    {
        return new Object[][]
            {
                {new String[] {"1"}},
                {new String[] {"1", "2"}}
            };
    }
}
