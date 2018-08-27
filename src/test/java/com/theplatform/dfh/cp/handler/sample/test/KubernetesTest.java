package com.theplatform.dfh.cp.handler.sample.test;

import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.handler.sample.api.ActionParamKeys;
import com.theplatform.dfh.cp.handler.sample.api.SampleAction;
import com.theplatform.dfh.cp.handler.sample.api.SampleActions;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;
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

import java.util.Collections;
import java.util.function.Consumer;

public class KubernetesTest extends SampleHandlerTestBase
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesTest.class);

    @Test
    public void runBasicTest()
    {
        SampleAction sampleAction = new SampleAction();
        sampleAction.setAction(SampleActions.log.name());
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(ActionParamKeys.logMessage, "This is the K8Test!");
        sampleAction.setParamsMap(paramsMap);
        SampleInput sampleInput = new SampleInput();
        sampleInput.setActions(Collections.singletonList(sampleAction));
        ParamsMap resultMap = new ParamsMap();
        resultMap.put("bitrate", 500);
        resultMap.put("filename", "coolest_dog.mp4");
        sampleInput.setResultPayload(resultMap);

        String payload = jsonHelper.getJSONString(sampleInput);
        logger.info("Payload: {}", payload);

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .addEnvVar("PAYLOAD", jsonHelper.getJSONString(sampleInput))
            .addEnvVar("LOG_LEVEL", "DEBUG");

        executionConfig.setCpuRequestModulator(new CpuRequestModulator()
        {
            @Override
            public String getCpuRequest()
            {
                return podConfig.getCpuMinRequestCount();
            }
        });

        podConfig.setPullAlways(true);

        PodFollower<PodPushClient> follower = new PodFollowerImpl<PodPushClient>(kubeConfig, podConfig, executionConfig);

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

            lastPodPhase = follower.startAndFollowPod(logLineObserver);

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
