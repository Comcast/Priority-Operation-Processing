package com.comcast.pop.modules.kube.fabric8.test;

import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.test.factory.DefaultRequestModulatorFactory;
import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.logging.LogLineObserver;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClientImpl;
import com.comcast.pop.modules.kube.fabric8.client.exception.PodException;
import com.comcast.pop.modules.kube.fabric8.client.exception.PodNotScheduledException;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollower;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.comcast.pop.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import com.comcast.pop.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.Pod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static com.comcast.pop.modules.kube.fabric8.test.factory.DefaultLogLineObserverFactory.getLogLineObserver;

/**
 * Connect to a live K8 instance
 */
public class LiveKubernetesTest extends KubeClientTestBase
{
    private static Logger logger = LoggerFactory.getLogger(LiveKubernetesTest.class);

    // TODO: if this test of a local cert file is required figure out how to do that...
    @Test(enabled = false)
    public void testOath()
    {
        KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();

        final String CERT_PATH = "~/ffmpeg.ca.cert";

        if (new File(CERT_PATH).exists())
        {
            kubeConfig.setOauthToken(readFile("~/ffmpeg.sa.token"));
            kubeConfig.setCaCertData(readFile("~/ffmpeg.ca.cert"));
        }
        else
        {
            Assert.fail(String.format("The cert file [%1$s] was not found!", CERT_PATH));
        }

        PodConfig podConfig = TestPodConfigType.quickPod.createPodConfig(configFactory);
        podConfig.setNamePrefix("kubeclient-testoath");

        CpuRequestModulator cpuRequestModulator = DefaultRequestModulatorFactory.getSimpleCpuRequestModulator(podConfig);

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(cpuRequestModulator);

        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);

        Assert.assertTrue(lastPhase.phase.equals(PodPhase.SUCCEEDED));
    }

    @DataProvider(name = "imageCommands")
    private Object[][] getImageCommands()
    {
        return new Object[][] {
            { TestPodConfigType.quickPod, 0, "QuickSuccess" },
            { TestPodConfigType.longerExecutionPod, 0, "LongSuccess" },
            { TestPodConfigType.longerExecutionFailsFastPod, 1, "QuickFail" }
        };
    }

    @Test(dataProvider = "imageCommands")
    public void testNewClient(TestPodConfigType podConfigCreator, int exitCode, String testName)
    {
        PodConfig podConfig = podConfigCreator.createPodConfig(configFactory);

        podConfig.setReapCompletedPods(true);
        podConfig.setNamePrefix(testName.toLowerCase());
        podConfig.setEndOfLogIdentifier("Splitting the commandline.");
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(DefaultRequestModulatorFactory.getSimpleCpuRequestModulator(podConfig));

        PodFollowerImpl follower = new PodFollowerImpl(configFactory.getDefaultKubeConfig(), podConfig, executionConfig);

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
    public void testLogTimeout()
    {
        Map<String, String> envVars = new HashMap<>();
        //final int runTime =  2 * 60 * 1000;
        // This is a way to force at least 2 logwatch resets to take place
        final int runTime =  20000;
        final int delayTime = 30000;
        final long timeout = 10000L;
        envVars.put("DEFAULT_EXEC_TIME", String.valueOf(runTime));
        envVars.put("DEFAULT_PRINT_DELAY", String.valueOf(delayTime));
        PodConfig podConfig = TestPodConfigType.printPod.createPodConfig(configFactory)
            .setEnvVars(envVars)
            .setPodStdoutTimeout(timeout)
            ;

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(DefaultRequestModulatorFactory.getSimpleCpuRequestModulator(podConfig));

        KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();
        loadCertsFromEnvironment(kubeConfig);
        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        try
        {
            follower.startAndFollowPod(logLineObserver);
            Assert.fail("Test should have timed out!");
        }
        catch(PodException e)
        {
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getClass(), TimeoutException.class);
            Assert.assertTrue(StringUtils.containsIgnoreCase(e.getCause().getMessage(), "log tail indicates no log activity"));
        }
        finally
        {
            try
            {
                follower.getPodPushClient().deletePod(executionConfig.getName());
            }
            catch (Exception e)
            {
                logger.error("Failed to clean up pod: {}", executionConfig.getName());
            }
        }
    }

    @Test(enabled = false)
    public void testLotsOfLogs()
    {
        Map<String, String> envVars = new HashMap<>();
        //final int runTime =  2 * 60 * 1000;
        // This is a way to force at least 2 logwatch resets to take place
        final int runTime =  30000;
        final int delayTime = 0;
        envVars.put("DEFAULT_EXEC_TIME", String.valueOf(runTime));
        envVars.put("DEFAULT_PRINT_DELAY", String.valueOf(delayTime));
        PodConfig podConfig = configFactory.getDefaultPodConfig()
            .setImageName("your.docker.repo.com/printalot:1.0.0")
            .setNamePrefix("k8clienttest")
            .setEnvVars(envVars)
            .setEndOfLogIdentifier("POPComplete")
        ;

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(DefaultRequestModulatorFactory.getSimpleCpuRequestModulator(podConfig));

        podConfig.setReapCompletedPods(false);
        KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();
        loadCertsFromEnvironment(kubeConfig);
        PodFollowerImpl follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        logLineObserver.addConsumer(new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                logger.trace(s);
            }
        });

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);
        Assert.assertEquals(PodPhase.SUCCEEDED, lastPhase.phase);
    }

    @Test
    public void testErrorMidExecution()
    {
        PodConfig podConfig = TestPodConfigType.longerExecutionPod.createPodConfig(configFactory);
        SimpleCpuRequestModulator simpleCpuRequestModulator = DefaultRequestModulatorFactory.getSimpleCpuRequestModulator(podConfig);
        podConfig.getAliveCheckDetails().setAliveCheckHost("10.10.10.10");
        podConfig.getAliveCheckDetails().setAliveCheckLinking(true);


        ///////////////////////////////////////////////////////////////////
        // slow it down WARN! this test is time sensitive, it needs the transcode to go slowly enough that alive check fails
        simpleCpuRequestModulator.setCpuLimit("1000m");
        simpleCpuRequestModulator.setCpuRequest("1000m");
        podConfig.getAliveCheckDetails().setAliveCheckFailureThreshold(3);
        podConfig.getAliveCheckDetails().setAliveCheckInterval(1);
        ///////////////////////////////////////////////////////////////////

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(simpleCpuRequestModulator);

        PodFollowerImpl follower = new PodFollowerImpl(configFactory.getDefaultKubeConfig(), podConfig, executionConfig);
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
        HiLowCpuRequestModulator hiLowCpuRequestModulator = DefaultRequestModulatorFactory.getHiLowCpuRequestModulator();

        class FollowerRunner implements Runnable
        {
            @Override
            public void run()
            {
                PodConfig podConfig = TestPodConfigType.longerExecutionPod.createPodConfig(configFactory);
                KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();
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
                    Pod pod = getPod(follower, kubeConfig.getNameSpace(), executionConfig.getName());
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
    public void testUnschedulable()
    {
        PodConfig podConfig = TestPodConfigType.quickPod.createPodConfig(configFactory);
        KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();

        CpuRequestModulator cpuRequestModulator = new CpuRequestModulator()
        {
            final String EXCESSIVE_CPU = "64000m";

            @Override
            public String getCpuRequest()
            {
                return EXCESSIVE_CPU;
            }

            @Override
            public String getCpuLimit()
            {
                return EXCESSIVE_CPU;
            }
        };

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(cpuRequestModulator);

        PodFollower<PodPushClient> follower = new PodFollowerImpl<>(configFactory.getDefaultKubeConfig(), podConfig, executionConfig);
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
            Pod pod = getPod(follower, kubeConfig.getNameSpace(), executionConfig.getName());
            Assert.assertTrue(pod == null);
        }
    }



    @Test
    public void testNewDisabledDelete()
    {
        KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();

        PodConfig podConfig = TestPodConfigType.quickPod.createPodConfig(configFactory);

        podConfig.setReapCompletedPods(false);
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodFollower follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);

        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);

        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);

        Pod pod = getPod(follower, kubeConfig.getNameSpace(), lastPhase.name);
        Assert.assertNotNull(pod);

        Assert.assertTrue(follower.getPodPushClient().deletePod(lastPhase.name));

        pod = getPod(follower, kubeConfig.getNameSpace(), lastPhase.name);
        Assert.assertNull(pod);
    }

    @Test
    public void testAllStdoutMaters_on_success_noInfiniteLoop()
    {
        KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();

        PodConfig podConfig = TestPodConfigType.longerExecutionSucceedsPod.createPodConfig(configFactory);

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodFollower follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);

        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);
        podConfig.setEndOfLogIdentifier("NEVER_FIND_ME_STDOUT");
        FinalPodPhaseInfo lastPhase = follower.startAndFollowPod(logLineObserver);
        // just being able to exit is enough, for awhile this test case was an infinite loop.
    }

    @Test
    public void testAllStdoutMaters_on_failure_noInfiniteLoop()
    {
        KubeConfig kubeConfig = configFactory.getDefaultKubeConfig();

        PodConfig podConfig = TestPodConfigType.longerExecutionSucceedsPod.createPodConfig(configFactory);

        podConfig.setImageName("bash")
            .setArguments(new String[]{"-c", "sleep 20 && echo asdfasdfasdf && exit 1"})
            .setNamePrefix("pop-failslow");
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodFollower follower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);

        LogLineObserver logLineObserver = getLogLineObserver(executionConfig, follower);
        podConfig.setEndOfLogIdentifier("NEVER_FIND_ME_STDOUT");
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

    public Pod getPod(PodFollower follower, String namespace, String podName)
    {
        return ((PodPushClientImpl) follower.getPodPushClient()).getKubernetesHttpClients().getRequestClient().getPodResource(namespace, podName).get();
    }
}
