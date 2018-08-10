package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;

public class DockerClientFactory
{
    public DockerClient createDockerClient(String dockerClientUrl)
    {
        return new DefaultDockerClient(dockerClientUrl);
    }
}