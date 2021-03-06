package com.comcast.pop.modules.kube.fabric8.test;


import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.LogLineAccumulator;
import com.comcast.pop.modules.kube.client.config.ConfigMapDetails;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KeyPathPair;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.comcast.pop.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.comcast.pop.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LiveKubernetesConfigMapAndEnvVars extends KubeClientTestBase
{
    public static final String IMAGE_NAME = "ubuntu:14.04";
    private static Logger logger = LoggerFactory.getLogger(LiveKubernetesConfigMapAndEnvVars.class);

    @Test
    public void testEnvVars() throws Exception
    {
        PodConfig podConfig = new PodConfig().applyDefaults();
        podConfig.setImageName("ubuntu:14.04");
        podConfig.setArguments(new String[] { "printenv" });
        podConfig.setNamePrefix("test-envvar");
        podConfig.setUseTaintedNodes(false);
        podConfig.setServiceAccountName("pop-service");
        podConfig.setDefaultEmptyDirLogging(true);
        podConfig.setReapCompletedPods(true);

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.addEnvVar("LOG_LEVEL", "DEBUG");

        CpuRequestModulator cpuModulator = new HiLowCpuRequestModulator();
        executionConfig.setCpuRequestModulator(cpuModulator);
        LogLineAccumulator logLineAccumulator = new LogLineAccumulatorImpl();
        executionConfig.setLogLineAccumulator(logLineAccumulator);

        PodPushClient podPushClient = new PodPushClientFactoryImpl().getClient(configFactory.getDefaultKubeConfig());

        try
        {
            CountDownLatch podSched = new CountDownLatch(1);
            CountDownLatch podFinished = new CountDownLatch(1);
            PodWatcher podWatcher = podPushClient.start(podConfig, executionConfig, podSched, podFinished);

            boolean isFinished;
            do
            {
                logger.info("Checking pod finished status for pod {}", executionConfig.getName());
                isFinished = podFinished.await(2L, TimeUnit.SECONDS);
            }
            while (!isFinished);

            logger.info("Pod finished w/{}", podWatcher.getFinalPodPhaseInfo());
            String logs = getString(logLineAccumulator);
            Assert.assertTrue(logs.contains("LOG_LEVEL=DEBUG"), "Logs should have contained the environment variable");
        }
        finally
        {
            podPushClient.deletePod(executionConfig.getName());
            podPushClient.close();
        }

    }

    private String getString(LogLineAccumulator logLineAccumulator)
    {
        StringBuilder stringBuilder = new StringBuilder();
        logLineAccumulator.takeAll().forEach(stringBuilder::append);
        String logs = stringBuilder.toString();
        logger.info(logs);
        return logs;
    }
}
