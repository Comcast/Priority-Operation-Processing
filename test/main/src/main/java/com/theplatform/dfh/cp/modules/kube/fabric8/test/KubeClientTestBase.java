package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import static com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory.getDefaultPodConfig;

/**
 * User: kimberly.todd
 * Date: 8/23/18
 */
public class KubeClientTestBase
{

    public final KubeConfig kubeConfig = DefaultConfigFactory.getDefaultKubeConfig();

    public PodConfig quickPod;
    public PodConfig longerExecutionPod;
    public PodConfig longerExecutionSucceedsPod;
    public PodConfig longerExecutionFailsFastPod;

    @BeforeMethod
    public void setUpPodConfigs()
    {
        quickPod = getDefaultPodConfig()
            .setImageName("docker-lab.repo.theplatform.com/mediainfo:1.0")
            .setNamePrefix("dfhk8clienttest")
            .setArguments(new String[] { "--Output=XML", "-f", "/testFiles/vault/podtest.mp4" })
            .setEndOfLogIdentifier("</Mediainfo>");

        longerExecutionFailsFastPod = getDefaultPodConfig()
            .setImageName("docker-lab.repo.theplatform.com/ffmpeg-test:3.1-centos")
            .setNamePrefix("dfhffmpeg-test")
            .setArguments(new String[] {
                "-i", "shortInsideContainer.mp4", "shortInsideContainer.out.mp4", "-y", "-loglevel", "debug" });

        longerExecutionSucceedsPod = getDefaultPodConfig()
            .setImageName("bash")
            .setArguments(new String[]{"-c", "sleep 20 && echo asdfasdfasdf && exit 0"})
            .setNamePrefix("dfh-sleep");

        longerExecutionPod = getDefaultPodConfig()
            .setImageName("docker-lab.repo.theplatform.com/ffmpeg-test:3.1-centos")
            .setNamePrefix("dfhffmpeg-test")
            .setArguments(new String[] {
                "-i", "/var/tmp/shortInsideContainer.mp4", "/var/tmp/shortInsideContainer.out.mp4", "-y", "-loglevel",
                "debug" });
    }
}
