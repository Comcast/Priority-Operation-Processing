package com.theplatform.dfh.cp.modules.kube.fabric8.client;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class PodPushClientImplTest
{

    @DataProvider(name = "start-params")
    Object[][] getCases()
    {
        return new Object[][] {
            { null, new ExecutionConfig() },
            { new PodConfig(), null },
            { new PodConfig(), new ExecutionConfig() },
        };
    }

    @Test(dataProvider = "start-params", expectedExceptions = IllegalArgumentException.class)
    void testNullStartParameters(PodConfig podConfig, ExecutionConfig executionConfig)
    {
        PodPushClient podPushClient = new PodPushClientImpl();

        podPushClient.start(podConfig, executionConfig, new CountDownLatch(1), new CountDownLatch(1));
    }

}
