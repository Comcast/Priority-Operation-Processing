package com.comcast.pop.modules.kube.fabric8.test;

import com.comcast.pop.modules.kube.fabric8.test.factory.DefaultConfigFactory;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.test.factory.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

public class KubeClientTestBase
{
    private static Logger logger = LoggerFactory.getLogger(KubeClientTestBase.class);
    final ResourceReader resourceReader;
    final ConfigFactory configFactory;

    public KubeClientTestBase()
    {
        resourceReader = new ResourceReader(null);
        configFactory = new DefaultConfigFactory(resourceReader);
    }

    /**
     * This contains the basic pods config types used by the tests. This is primarily to avoid the timing issues of a DataProvider vs. BeforeClass/BeforeMethod.
     */
    enum TestPodConfigType implements PodConfigCreator
    {
        quickPod
            {
                @Override
                public PodConfig createPodConfig(ConfigFactory configFactory)
                {
                    return configFactory.getDefaultPodConfig()
                        .setImageName("your.docker.repo.com/mediainfo:1.0")
                        .setNamePrefix("k8clienttest")
                        .setArguments(new String[] { "--version" })
                        .setEndOfLogIdentifier("MediaInfoLib");
                }
            },
        longerExecutionFailsFastPod
            {
                @Override
                public PodConfig createPodConfig(ConfigFactory configFactory)
                {
                    return configFactory.getDefaultPodConfig()
                        .setImageName("your.docker.repo.com/ffmpeg-test:3.1-centos")
                        .setNamePrefix("ffmpeg-test")
                        .setArguments(new String[] {
                            "-i", "shortInsideContainer.mp4", "shortInsideContainer.out.mp4", "-y", "-loglevel", "debug" });
                }
            },
        longerExecutionSucceedsPod
            {
                @Override
                public PodConfig createPodConfig(ConfigFactory configFactory)
                {
                    return configFactory.getDefaultPodConfig()
                        .setImageName("bash")
                        .setArguments(new String[]{"-c", "sleep 20 && echo asdfasdfasdf && exit 0"})
                        .setNamePrefix("pop-sleep");
                }
            },
        longerExecutionPod
            {
                @Override
                public PodConfig createPodConfig(ConfigFactory configFactory)
                {
                    return configFactory.getDefaultPodConfig()
                        .setImageName("your.docker.repo.com/ffmpeg-test:3.1-centos")
                        .setNamePrefix("ffmpeg-test")
                        .setCpuMinRequestCount("4000m")
                        .setCpuMaxRequestCount("8000m")
                        .setArguments(new String[] {
                            "-i", "/var/tmp/shortInsideContainer.mp4", "/var/tmp/shortInsideContainer.out.mp4", "-y", "-loglevel",
                            "debug" });
                }
            },
        printPod
            {
                @Override
                public PodConfig createPodConfig(ConfigFactory configFactory)
                {
                    return configFactory.getDefaultPodConfig()
                        .setImageName("your.docker.repo.com/printalot:1.0.0")
                        .setNamePrefix("pop-print-test")
                        .setEndOfLogIdentifier("HandlerComplete")
                        ;
                }
            }
    }

    protected void loadCertsFromEnvironment(KubeConfig kubeConfig)
    {
        String oauthCertPath = System.getenv("oauthCertPath");
        if(oauthCertPath != null)
            kubeConfig.setCaCertData(readFileIntoString(oauthCertPath));

        String oauthTokenPath = System.getenv("oauthTokenPath");
        if(oauthTokenPath != null)
            kubeConfig.setOauthToken(readFileIntoString(oauthTokenPath));
    }

    private String readFileIntoString(String filePath)
    {
        try
        {
            return new String(Files.readAllBytes(new File(filePath).toPath()));
        }
        catch(Exception e)
        {
            logger.error("Failed to read file: " + filePath, e);
        }
        return null;
    }
}
