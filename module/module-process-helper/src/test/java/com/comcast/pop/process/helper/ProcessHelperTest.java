package com.comcast.pop.process.helper;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ProcessHelperTest
{
    @Test(enabled=false)
    public void testLSExec() throws Exception
    {
        ProcessHelper processHelper = new ProcessHelper();
        ProcessOutput processOutput = processHelper.getOutput("ls", null, null);
        Assert.assertNotEquals(0, processOutput.getOutputLines().size());
    }
}
