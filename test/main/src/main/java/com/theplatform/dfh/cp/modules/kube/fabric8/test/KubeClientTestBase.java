package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory;

/**
 * User: kimberly.todd
 * Date: 8/23/18
 */
public class KubeClientTestBase
{

    public final KubeConfig kubeConfig = DefaultConfigFactory.getDefaultKubeConfig();

    /**
     * This contains the basic pods config types used by the tests. This is primarily to avoid the timing issues of a DataProvider vs. BeforeClass/BeforeMethod.
     */
    enum TestPodConfigType implements PodConfigCreator
    {
        quickPod
            {
                @Override
                public PodConfig createPodConfig()
                {
                    return DefaultConfigFactory.getDefaultPodConfig()
                        .setImageName("docker-lab.repo.theplatform.com/mediainfo:1.0")
                        .setNamePrefix("dfhk8clienttest")
                        .setArguments(new String[] { "--version" })
                        .setEndOfLogIdentifier("MediaInfoLib");
                }
            },
        longerExecutionFailsFastPod
            {
                @Override
                public PodConfig createPodConfig()
                {
                    return DefaultConfigFactory.getDefaultPodConfig()
                        .setImageName("docker-lab.repo.theplatform.com/ffmpeg-test:3.1-centos")
                        .setNamePrefix("dfhffmpeg-test")
                        .setArguments(new String[] {
                            "-i", "shortInsideContainer.mp4", "shortInsideContainer.out.mp4", "-y", "-loglevel", "debug" });
                }
            },
        longerExecutionSucceedsPod
            {
                @Override
                public PodConfig createPodConfig()
                {
                    return DefaultConfigFactory.getDefaultPodConfig()
                        .setImageName("bash")
                        .setArguments(new String[]{"-c", "sleep 20 && echo asdfasdfasdf && exit 0"})
                        .setNamePrefix("dfh-sleep");
                }
            },
        longerExecutionPod
            {
                @Override
                public PodConfig createPodConfig()
                {
                    return DefaultConfigFactory.getDefaultPodConfig()
                        .setImageName("docker-lab.repo.theplatform.com/ffmpeg-test:3.1-centos")
                        .setNamePrefix("dfhffmpeg-test")
                        .setCpuMinRequestCount("4000m")
                        .setCpuMaxRequestCount("8000m")
                        .setArguments(new String[] {
                            "-i", "/var/tmp/shortInsideContainer.mp4", "/var/tmp/shortInsideContainer.out.mp4", "-y", "-loglevel",
                            "debug" });
                }
            }
    }
}
