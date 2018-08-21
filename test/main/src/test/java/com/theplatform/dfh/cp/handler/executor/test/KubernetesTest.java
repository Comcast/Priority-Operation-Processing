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

public class KubernetesTest extends ExecutorHandlerTestBase
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesTest.class);

    // This is a hard coded manual test!!! Enjoy!!

    @Test(enabled = false)
    public void run() throws Exception
    {
        PodFollower<PodPushClient> follower = new PodFollowerImpl<>(kubeConfig);

        // TODO: environment variable passing with the $$ is messed up (have to use $$$, pick a new char/flag/indicator OR base64 encode?)
        String payload = getStringFromResourceFile("/payload/sampleActions.json");
        logger.info("Payload: {}", payload);

        // if this is not set the pod annotations cannot be written to
        podConfig.setServiceAccountName("ffmpeg-service");

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .addEnvVar("PAYLOAD", payload)
            .addEnvVar("LOG_LEVEL", "DEBUG");

        podConfig.setArguments(new String[] {"-launchType", "local"});

        podConfig.setConfigMapDetails(new ConfigMapDetails()
            .setConfigMapName("lab-main-t-aor-fhexec-t01")
            .setMapKey("external-properties")
            .setMapPath("external.properties")
            .setVolumeName("config-volume")
            .setVolumeMountPath("/config"));

        executionConfig.setCpuRequestModulator(new CpuRequestModulator()
        {
            @Override
            public String getCpuRequest()
            {
                return podConfig.getCpuMinRequestCount();
            }
        });

        podConfig.setPullAlways(false);
        logger.debug("Executing mediaInfo w/details {}", executionConfig);
        String podName = executionConfig.getName();
        LogLineObserver logLineObserver = follower.getDefaultLogLineObserver(executionConfig);

        logger.info("Getting progress until the pod {} is finished.", podName);
        StringBuilder allStdout = new StringBuilder();
        logLineObserver.addConsumer(new Consumer<String>()
                                    {
                                        @Override
                                        public void accept(String s)
                                        {
                                            logger.info("STDOUT: {}", s);
                                        }
                                    });

        FinalPodPhaseInfo lastPodPhase = null;
        try
        {
            logger.info("Starting the pod with name {}", podName);

            lastPodPhase = follower.startAndFollowPod(podConfig, executionConfig, logLineObserver);

            logger.info("Sample completed with pod status {}", lastPodPhase.phase.getLabel());
            if (lastPodPhase.phase.hasFinished())
            {
                if (lastPodPhase.phase.isFailed())
                {
                    logger.error("Sample failed to produce metadata, output was : {}", allStdout);
                    throw new RuntimeException(allStdout.toString());
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Sample produced: {}", allStdout.toString());
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
