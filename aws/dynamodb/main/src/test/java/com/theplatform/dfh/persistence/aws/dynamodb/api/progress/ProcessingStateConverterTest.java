package com.theplatform.dfh.persistence.aws.dynamodb.api.progress;

import com.theplatform.dfh.cp.api.progress.ProcessingState;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 */
public class ProcessingStateConverterTest
{

    @DataProvider(name = "processingStates")
    Object[][] getProcessingStates()
    {
        return new Object[][] {
            {ProcessingState.WAITING, "waiting"},
            {ProcessingState.COMPLETE, "CompLETe"},
            {ProcessingState.EXECUTING, "execUTING"}
        };
    }

    @Test(dataProvider = "getProcessingStates")
    void testUnconvert(ProcessingState expected, String value)
    {
        ProcessingStateConverter converter = new ProcessingStateConverter();
        ProcessingState result = converter.unconvert(value);
        Assert.assertEquals(result, expected);
    }


}
