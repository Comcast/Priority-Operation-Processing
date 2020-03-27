package com.theplatform.dfh.cp.modules.kube.fabric8.test;


import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KeyPathPair;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
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
        podConfig.setServiceAccountName("fission-service");
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

    //
    // Warning: this test is very specific to this config: https://github.comcast.com/VideoPlatformConfigurations/fhexec/blob/master/lab-main-t-aor-fhexec-t02/ConfigMap.yaml
    //
    @Test
    public void testProperties() throws Exception
    {
        PodConfig podConfig = new PodConfig().applyDefaults();
        podConfig.setImageName(IMAGE_NAME);
        podConfig.setArguments(new String[] { "cat", "/config/external.properties" });
        podConfig.setNamePrefix("testprop");

        podConfig.setConfigMapSettings(Collections.singletonList(new ConfigMapDetails()
            .setMapKeyPaths(Collections.singletonList(new KeyPathPair("external-properties", "external.properties")))
            .setConfigMapName(resourceReader.getValue("configMap"))
            .setVolumeName("config-volume")
            .setVolumeMountPath("/config")));

        podConfig.setUseTaintedNodes(false);
        podConfig.setServiceAccountName("fission-service");
        podConfig.setDefaultEmptyDirLogging(true);

        LogLineAccumulatorImpl logLineAccumulator = new LogLineAccumulatorImpl();
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setLogLineAccumulator(logLineAccumulator);
        executionConfig.setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodPushClient client = new PodPushClientFactoryImpl().getClient(configFactory.getDefaultKubeConfig());

        try
        {
            CountDownLatch podSched = new CountDownLatch(1);
            CountDownLatch podFinished = new CountDownLatch(1);
            PodWatcher podWatcher = client.start(podConfig, executionConfig, podSched, podFinished);

            boolean isFinished;
            do
            {
                logger.info("Checking pod finished status for pod {}", executionConfig.getName());
                isFinished = podFinished.await(2L, TimeUnit.SECONDS);
            }
            while (!isFinished);

            String logs = getString(logLineAccumulator);
            Assert.assertTrue(logs.contains("idm.url=http://identity.auth.test.corp.theplatform.com/idm"));
            logger.info("Pod finished w/{}", podWatcher.getFinalPodPhaseInfo());
            client.deletePod(executionConfig.getName());
        }
        finally
        {
            client.deletePod(executionConfig.getName());
            client.close();
        }
    }
}
