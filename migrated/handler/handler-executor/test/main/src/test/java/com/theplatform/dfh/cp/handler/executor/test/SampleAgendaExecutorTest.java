package com.theplatform.dfh.cp.handler.executor.test;

import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.function.Consumer;

/**
 * Runs the executor in kubernetes with a basic set of sample actions
 */
public class SampleAgendaExecutorTest extends ExecutorHandlerTestBase
{
    private static Logger logger = LoggerFactory.getLogger(SampleAgendaExecutorTest.class);

    @Test
    public void basicSampleExecutorTest() throws Exception
    {
        // TODO: environment variable passing with the $$ is messed up (have to use $$$, pick a new char/flag/indicator OR base64 encode?)
//        String payload = getStringFromResourceFile("/payload/sampleActions.json");
//        String payload = getStringFromResourceFile("/payload/filterAgenda.json");
        String payload = getStringFromResourceFile("/payload/immortalsPublishAgenda.json");

        // if this is not set the pod annotations cannot be written to
        podConfig.setServiceAccountName("ffmpeg-service");

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .addEnvVar("PAYLOAD", payload)
            .addEnvVar("LOG_LEVEL", "DEBUG");


        executionConfig.setCpuRequestModulator(new CpuRequestModulator()
        {
            @Override
            public String getCpuRequest()
            {
                return podConfig.getCpuMinRequestCount();
            }

            @Override
            public String getCpuLimit()
            {
                return podConfig.getCpuMaxRequestCount();
            }
        });

        String podName = executionConfig.getName();

        PodFollower<PodPushClient> follower = new PodFollowerImpl<>(kubeConfig, podConfig, executionConfig);

        logger.info("Getting progress until the pod {} is finished.", podName);
        StringBuilder allStdout = new StringBuilder();
        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);
        logLineObserver.addConsumer(new Consumer<String>()
                                    {
                                        @Override
                                        public void accept(String s)
                                        {
                                            logger.info("STDOUT: {}", s);
                                        }
                                    });

        FinalPodPhaseInfo lastPodPhase;
        try
        {
            logger.info("Starting the pod with name {}", podName);

            lastPodPhase = follower.startAndFollowPod(logLineObserver);

            logger.info("Executor completed with pod status {}", lastPodPhase.phase.getLabel());
            if (lastPodPhase.phase.hasFinished())
            {
                if (lastPodPhase.phase.isFailed())
                {
                    logger.error("Executor failed to produce metadata, output was : {}", allStdout);
                    throw new RuntimeException(allStdout.toString());
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Executor produced: {}", allStdout.toString());
            }
        }
        catch (Exception e)
        {
            String allStringMetadata = allStdout.toString();
            logger.error("Exception caught {}", allStringMetadata, e);
            throw new RuntimeException(allStringMetadata, e);
        }
        logger.info("Done");
    }
}
