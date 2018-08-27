package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory;
import com.theplatform.test.dfh.filemanager.client.TestFileManagerClient;
import com.theplatform.test.modules.resourcereader.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory.getDefaultPodConfig;
import static com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultLogLineObserverFactory.getChattyLogLineObserver;
import static com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultLogLineObserverFactory.getLogLineObserver;

/**
 * User: kimberly.todd
 * Date: 8/27/18
 */
public class SplitAHugeMp4IntoSmallerFileTest extends KubeClientTestBase
{

    private static Logger logger = LoggerFactory.getLogger(LiveKubernetesTest.class);

    private TestFileManagerClient tfmClient;
    private KubeConfig kubeConfig = DefaultConfigFactory.getDefaultKubeConfig();

    private PodConfig longerPodConfig;
    private PodConfig longerPodConfigFast;

    private ResourceReader resourceReader;

    String propertyPrefix = "dev";


    @BeforeMethod
    public void setUp()
    {
        resourceReader = new ResourceReader(propertyPrefix);
        String tfmHost = resourceReader.getValue("tfmhost");
        tfmClient = new TestFileManagerClient(tfmHost);

        longerPodConfig = getDefaultPodConfig()
            .setImageName("docker-lab.repo.theplatform.com/ffmpeg-perm:1.2")
            .setNamePrefix("dfhffmpeg-test")
            .setArguments(new String[] {
                "-i", "/testFiles/vault/ep_large_feature.mp4", "-vframes", "10000",
                "/testFiles/vault/ep_large_feature" + "-test" + "-out.mp4", "-y", "-loglevel", "debug" });

        longerPodConfigFast = getDefaultPodConfig()
            .setImageName("docker-lab.repo.theplatform.com/ffmpeg-perm:1.2")
            .setNamePrefix("dfhffmpeg-test")
            .setArguments(new String[] {
                "-i", "/testFiles/vault/ep_large_feature.mp4", "-vcodec", "copy", "-acodec", "copy", "-ss", "00:00:00",
                "-t", "00:30:00", "/testFiles/vault/ep_large_feature" + "-test2" + "-out.mp4", "-y", "-loglevel",
                "debug" });
    }

    @Test
    public void testName() throws Exception
    {
        PodConfig podConfig = longerPodConfig;
        HiLowCpuRequestModulator hiLowCpuRequestModulator = new HiLowCpuRequestModulator();
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(hiLowCpuRequestModulator);

        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);
    }

    @Test
    public void testFastChop() throws Exception
    {
        PodConfig podConfig = longerPodConfigFast;
        HiLowCpuRequestModulator hiLowCpuRequestModulator = new HiLowCpuRequestModulator();

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(hiLowCpuRequestModulator);

        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getChattyLogLineObserver(executionConfig, follower, 100);

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);

    }
}
