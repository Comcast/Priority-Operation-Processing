package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: kimberly.todd
 * Date: 8/24/18
 */
public class PodNameTest extends KubeClientTestBase
{
    private static Logger logger = LoggerFactory.getLogger(OAuthTest.class);

    @Test
    public void testPodName_ExternalId_CanBeSeen() throws Exception
    {

        PodConfig podConfig = new PodConfig().applyDefaults();
        podConfig.setImageName("ubuntu:14.04");
        podConfig.setArguments(new String[] { "printenv" });
        podConfig.setNamePrefix("testpodname");
        podConfig.setReapCompletedPods(false);

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.addEnvVar("LOG_LEVEL", "DEBUG");
        executionConfig.setCpuRequestModulator(new HiLowCpuRequestModulator());

        PodFollower podFollower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig);
        podFollower.startAndFollowPod(podFollower.getDefaultLogLineObserver(executionConfig));

        PodResource<Pod, DoneablePod> p = podFollower.getPodPushClient().getFabric8Client()
            .inNamespace(kubeConfig.getNameSpace()).pods()
            .withName(executionConfig.getName());
        Thread.sleep(10000);

        try
        {
            Assert.assertNotNull(p);
            Map<String, String> labels = p.get().getMetadata().getLabels();
            Assert.assertTrue(labels.containsKey(Fabric8Helper.EXTERNAL_ID));
            Assert.assertTrue(labels.containsKey(Fabric8Helper.EXTERNAL_GROUP_ID));

            List<EnvVar> env = p.get().getSpec().getContainers().get(0).getEnv();

            AtomicBoolean found = new AtomicBoolean();
            env.forEach(i ->
            {
                if (i.getName().equals(Fabric8Helper.MY_POD_NAME))
                {
                    found.set(true);
                }
            });
            Assert.assertTrue(found.get(), "Environment variable not found.");

            String stdoout = p.getLog();
            stdoout.contains(Fabric8Helper.MY_POD_NAME);
        }
        finally
        {
            podFollower.getPodPushClient().deletePod(executionConfig.getName());
        }
    }
}
