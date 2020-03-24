package com.theplatform.dfh.cp.handler.sample.test;

import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.params.VideoStreamParams;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.handler.sample.api.ActionParamKeys;
import com.theplatform.dfh.cp.handler.sample.api.SampleAction;
import com.theplatform.dfh.cp.handler.sample.api.SampleActions;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;
import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class KubernetesTest extends SampleHandlerTestBase
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesTest.class);
    private final String EXPECTED_ID = "theId";

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
        // just an example object to pass through
        VideoStreamParams videoStreamParams = new VideoStreamParams();
        videoStreamParams.setId(EXPECTED_ID);
        ParamsMap resultParamsMap = new ParamsMap();
        jsonHelper.getMapFromObject(videoStreamParams).forEach(resultParamsMap::put);
        sampleInput.setResultPayload(resultParamsMap);

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

        Map<String, String> podAnnotations = follower.getPodAnnotations();
        String successPayload = podAnnotations.get(KubernetesReporter.REPORT_SUCCESS_ANNOTATION);
        Assert.assertNotNull(successPayload, "Success payload was not populated by the handler!");
        VideoStreamParams resultVideoStreamParams = jsonHelper.getObjectFromString(successPayload, VideoStreamParams.class);
        Assert.assertEquals(resultVideoStreamParams.getId(), EXPECTED_ID);
        logger.info("Done");
    }
}
