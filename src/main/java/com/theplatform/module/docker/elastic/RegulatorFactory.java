package com.theplatform.module.docker.elastic;

import com.spotify.docker.client.DockerClient;
import com.theplatform.module.docker.client.DockerContainerRegulatorClient;

/**
 *
 */
public class RegulatorFactory
{
    public static InstanceRegulator getDockerRegulator(DockerClient dockerClient, String containerPrefix, String imageName)
    {
        InstanceRegulator instanceRegulator = new InstanceRegulator();
        DockerContainerRegulatorClient dockerContainerRegulatorClient = getDockerContainerRegulatorClient(
            dockerClient, containerPrefix, imageName);
        instanceRegulator.setInstanceRegulatorClient(dockerContainerRegulatorClient);
        return instanceRegulator;
    }

    public static DockerContainerRegulatorClient getDockerContainerRegulatorClient(DockerClient dockerClient,
        String containerPrefix, String imageName)
    {
        DockerContainerRegulatorClient dockerContainerRegulatorClient = new DockerContainerRegulatorClient();
        dockerContainerRegulatorClient.setContainerNamePrefix(containerPrefix);
        dockerContainerRegulatorClient.setImageName(imageName);
        dockerContainerRegulatorClient.setDockerClient(dockerClient);
        return dockerContainerRegulatorClient;
    }
}
