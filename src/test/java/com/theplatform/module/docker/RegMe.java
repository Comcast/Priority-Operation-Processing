package com.theplatform.module.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.theplatform.module.docker.elastic.RegulatorFactory;
import com.theplatform.module.docker.elastic.demand.DemandFactory;
import com.theplatform.module.docker.elastic.demand.RealTimeProxyConfig;
import com.theplatform.module.docker.elastic.demand.S3DemandClient;
import org.testng.annotations.Test;

/**
 *
 */
public class RegMe
{

    S3DemandClient s3DemandClient = new S3DemandClient();

    @Test(enabled = false)
    public void testExpirement() throws Exception
    {
        configureS3Client();

        RealTimeProxyConfig config = DemandFactory.getRealTimeProxyConfig(s3DemandClient);
        System.out.println(config.isMoreDesired());
    }

    private void configureS3Client()
    {
        s3DemandClient.setBucketName("tp-dfh");
        s3DemandClient.setKey("fuse-proxy/dev.demand.json");
        s3DemandClient.setKeyId("AKIAIZGYJADGPEO5FEPQ");
        s3DemandClient.setSecretKey("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");
    }

    @Test(enabled = false)
    public void testDoubleInstanceCreate() throws Exception
    {
        configureS3Client();
        DockerClient dockerClient;
        dockerClient = new DefaultDockerClient("http://devtpdfhcon01:2375/");
        DockerContainerRegulatorClient dockerContainerRegulatorClient = RegulatorFactory
            .getDockerContainerRegulatorClient(dockerClient, "fhc", "docker-lab.repo.theplatform.com/fhc:1.0.3");
        dockerContainerRegulatorClient.setLogLevel("DEBUG");
        dockerContainerRegulatorClient.setConfigVolume("/app/config:/app/config");
        dockerContainerRegulatorClient.setHeapSize("256m");
        dockerContainerRegulatorClient.setSecodsToWaitBeforeKill(3);
        dockerContainerRegulatorClient.setSecodsToWaitBeforeKillAll(10);
        dockerContainerRegulatorClient.setNetworkMode("bridge");
        dockerContainerRegulatorClient.startInstance("3");
    }
}
