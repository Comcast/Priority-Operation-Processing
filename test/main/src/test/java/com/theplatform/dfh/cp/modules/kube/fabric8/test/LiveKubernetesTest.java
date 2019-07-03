package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.client.config.*;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClientImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.exception.PodNotScheduledException;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory.getDefaultPodConfig;
import static com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultLogLineObserverFactory.getLogLineObserver;
import static com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultRequestModulatorFactory.getHiLowCpuRequestModulator;

/**
 * Connect to a live K8 instance
 */
public class LiveKubernetesTest extends KubeClientTestBase
{
    private static Logger logger = LoggerFactory.getLogger(LiveKubernetesTest.class);

    @Test
    public void testOath() throws Exception
    {
        if (new File("/depot/DFH/git/ffmpeg.ca.cert").exists())
        {
            kubeConfig.setOauthToken(readFile("/depot/DFH/git/ffmpeg.sa.token"));
            kubeConfig.setCaCertData(readFile("/depot/DFH/git/ffmpeg.ca.cert"));
        }

        PodConfig podConfig = quickPod;
        podConfig.setNamePrefix("kubeclient-testoath");

        HiLowCpuRequestModulator hiLowCpuRequestModulator = getHiLowCpuRequestModulator();

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(hiLowCpuRequestModulator);

        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);

        Assert.assertTrue(lastPhase.phase.equals(PodPhase.SUCCEEDED));
    }

    @DataProvider(name = "imageCommands")
    private Object[][] getImageCommands()
    {
        return new Object[][] {
            { quickPod, 0, "QuickSuccess" },
            { longerExecutionPod, 0, "LongSuccess" },
            { longerExecutionFailsFastPod, 1, "QuickFail" }
        };
    }

    @Test(dataProvider = "imageCommands")
    public void testNewClient(PodConfig podConfig, int exitCode, String testName)
        throws Exception
    {
        podConfig.setReapCompletedPods(true);
        podConfig.setNamePrefix(testName.toLowerCase());
        podConfig.setEndOfLogIdentifier("Splitting the commandline.");
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(getHiLowCpuRequestModulator());

        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);

        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);
        logLineObserver.addConsumer(new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                logger.info("POD LOGS: {}", s);
            }
        });

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);

        if (exitCode != 0)
        {
            Assert.assertTrue(lastPhase.phase.isFailed(), testName);
            Assert.assertEquals(new Integer(exitCode), lastPhase.exitCode, testName);
        }
        else
        {
            Assert.assertEquals(lastPhase.phase.getLabel(), PodPhase.SUCCEEDED.getLabel(), testName);
        }
    }

    @Test
    public void testErrorMidExecution() throws Exception
    {
        PodConfig podConfig = longerExecutionPod;
        HiLowCpuRequestModulator hiLowCpuRequestModulator = getHiLowCpuRequestModulator();
        podConfig.getAliveCheckDetails().setAliveCheckHost("10.10.10.10");
        podConfig.getAliveCheckDetails().setAliveCheckLinking(true);


        ///////////////////////////////////////////////////////////////////
        // slow it down WARN! this test is time sensitive, it needs the transcode to go slowly enough that alive check fails
        hiLowCpuRequestModulator.setMinimumCpuRequest("1000m");
        hiLowCpuRequestModulator.setMaximumCpuRequest("1000m");
        podConfig.getAliveCheckDetails().setAliveCheckFailureThreshold(3);
        podConfig.getAliveCheckDetails().setAliveCheckInterval(1);
        ///////////////////////////////////////////////////////////////////

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(hiLowCpuRequestModulator);

        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        // Uncomment for blow-by-blow info
//        logLineObserver.addConsumer(new Consumer<String>()
//        {
//            @Override
//            public void accept(String s)
//            {
//                logger.info(s);
//            }
//        });

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);

        Assert.assertTrue(lastPhase.phase.isFailed());
        Assert.assertEquals(new Integer(255), lastPhase.exitCode);
    }

    @Test
    public void testMultipleFollows() throws Exception
    {
        HiLowCpuRequestModulator hiLowCpuRequestModulator = getHiLowCpuRequestModulator();

        class FollowerRunner implements Runnable
        {
            @Override
            public void run()
            {
                PodConfig podConfig = longerExecutionPod;
                // set a reasonalby short wait time for the testing purposes
                podConfig.setPodScheduledTimeoutMs(20000L);
                ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
                executionConfig.setCpuRequestModulator(hiLowCpuRequestModulator);

                PodFollower<PodPushClient> follower = new PodFollowerImpl<>(kubeConfig, podConfig, executionConfig);

                FinalPodPhaseInfo finalPhase = null;
                try
                {
                    LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);
                    finalPhase = follower.startAndFollowPod(logLineObserver);
                }
                catch (Exception e)
                {

                }
                finally
                {
                    Pod pod = getPod(follower, executionConfig.getName());
                    Assert.assertTrue(pod == null);
                    Assert.assertTrue(finalPhase.phase.equals(PodPhase.SUCCEEDED));
                }
            }
        }

        FollowerRunner followerRunner = new FollowerRunner();
        FollowerRunner followerRunner2 = new FollowerRunner();

        Thread t = new Thread(followerRunner);
        t.start();

        Thread t2 = new Thread(followerRunner2);
        t2.start();

        t.join();
        t2.join();
    }

    @Test
    public void testUnschedulable() throws Exception
    {
        PodConfig podConfig = quickPod;

        HiLowCpuRequestModulator hiLowCpuRequestModulator = getHiLowCpuRequestModulator();

        // WAY TOO LARGE TO SCHEUDLE
        hiLowCpuRequestModulator.setMinimumCpuRequest("64000m");

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(hiLowCpuRequestModulator);

        PodFollower<PodPushClient> follower = new PodFollowerImpl<>(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        // set a reasonalby short wait time for the testing purposes
        podConfig.setPodScheduledTimeoutMs(20000L);
        
        try
        {
            FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);
            Assert.fail("Should have failed to schedule");
        }
        catch (Exception e)
        {
            Assert.assertTrue(PodNotScheduledException.class.isInstance(e.getCause()));
        }
        finally
        {
            Pod pod = getPod(follower, executionConfig.getName());
            Assert.assertTrue(pod == null);
        }
    }



    @Test
    public void testNewDisabledDelete()
        throws Exception
    {
        KubeConfig kubeConfig = DefaultConfigFactory.getDefaultKubeConfig();

        quickPod.setReapCompletedPods(false);
        ExecutionConfig executionConfig = new ExecutionConfig(quickPod.getNamePrefix())
            .setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodFollower follower = new PodFollowerImpl(kubeConfig, quickPod, executionConfig);

        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);

        Pod pod = getPod(follower, lastPhase.name);
        Assert.assertNotNull(pod);

        Assert.assertTrue(follower.getPodPushClient().deletePod(lastPhase.name));

        pod = getPod(follower, lastPhase.name);
        Assert.assertNull(pod);
    }

    @Test
    public void testAllStdoutMaters_on_success_noInfiniteLoop() throws Exception
    {
        KubeConfig kubeConfig = DefaultConfigFactory.getDefaultKubeConfig();

        ExecutionConfig executionConfig = new ExecutionConfig(longerExecutionSucceedsPod.getNamePrefix())
            .setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodFollower follower = new PodFollowerImpl(kubeConfig, longerExecutionSucceedsPod, executionConfig);

        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);
        longerExecutionSucceedsPod.setEndOfLogIdentifier("NEVER_FIND_ME_STDOUT");
        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);
        // just being able to exit is enough, for awhile this test case was an infinite loop.
    }

    @Test
    public void testAllStdoutMaters_on_failure_noInfiniteLoop() throws Exception
    {
        KubeConfig kubeConfig = DefaultConfigFactory.getDefaultKubeConfig();

        longerExecutionSucceedsPod.setImageName("bash")
            .setArguments(new String[]{"-c", "sleep 20 && echo asdfasdfasdf && exit 1"})
            .setNamePrefix("dfh-failslow");
        ExecutionConfig executionConfig = new ExecutionConfig(longerExecutionSucceedsPod.getNamePrefix())
            .setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodFollower follower = new PodFollowerImpl(kubeConfig, longerExecutionSucceedsPod, executionConfig);

        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);
        longerExecutionSucceedsPod.setEndOfLogIdentifier("NEVER_FIND_ME_STDOUT");
        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);
        // just being able to exit is enough, for awhile this test case was an infinite loop.
    }
    
    public static String readFile(String path)
    {
        String content = null;
        try
        {
            content = new String(Files.readAllBytes(Paths.get(path)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return content;
    }

    public Pod getPod(PodFollower follower, String podName)
    {
        return ((PodPushClientImpl) follower.getPodPushClient()).getPodResource(podName).get();
    }
}
