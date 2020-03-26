package com.theplatform.module.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.theplatform.module.docker.client.DockerContainerRegulatorClient;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 */
public class DockerContainerRegulatorClientTest
{
    private DockerContainerRegulatorClient client = new DockerContainerRegulatorClient();

    @Test
    public void testPullSkipping() throws Exception
    {
        DockerClient mock = Mockito.mock(DockerClient.class);

        when(mock.createContainer(any(ContainerConfig.class), anyString())).thenReturn(Mockito.mock(ContainerCreation.class));

        client.setDockerClient(mock);
        client.setSkipPull(true);
        client.startInstance("foo");

        verify(mock, atLeastOnce()).createContainer(any(ContainerConfig.class), anyString());
        verify(mock, times(0)).pull(anyString());
    }

    @Test
    public void testPullAllowed() throws Exception
    {
        DockerClient mock = Mockito.mock(DockerClient.class);

        when(mock.createContainer(any(ContainerConfig.class), anyString())).thenReturn(Mockito.mock(ContainerCreation.class));

        client.setDockerClient(mock);
        client.startInstance("foo");

        verify(mock, atLeastOnce()).createContainer(any(ContainerConfig.class), anyString());
        verify(mock, times(1)).pull(anyString());
    }
}
