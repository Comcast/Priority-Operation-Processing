package com.comcast.pop.modules.kube.fabric8.client.follower;

import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.fabric8.client.client.KubernetesHttpClients;
import com.comcast.pop.modules.kube.fabric8.client.factory.PodPushClientFactoryImpl;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PodFollowerImplTest
{
    private final String NAMESPACE = "theNamespace";
    private final String POD_NAME = "thePodName";
    private PodFollowerImpl podFollower;
    private KubeConfig kubeConfig;
    private PodConfig podConfig;
    private ExecutionConfig executionConfig;
    private KubernetesHttpClients mockClients;
    private PodPushClientFactoryImpl mockPushPodClientFactory;
    private PodPushClient mockPodPushClient;

    @BeforeMethod
    public void setup()
    {
        kubeConfig = new KubeConfig();
        podConfig = new PodConfig();
        executionConfig = new ExecutionConfig();
        mockClients = mock(KubernetesHttpClients.class, Mockito.RETURNS_DEEP_STUBS);
        mockPushPodClientFactory = mock(PodPushClientFactoryImpl.class);
        mockPodPushClient = mock(PodPushClient.class);
    }

    @DataProvider
    public Object[][] logTimeoutProvider()
    {
        return new Object[][]
            {
                { null, 30000L, false},
                { 0L, 30000L, true},
                { Instant.now().toEpochMilli(), 30000L, false},
                { Instant.now().minusSeconds(31).toEpochMilli(), 30000L, true}
            };
    }

    @Test(dataProvider = "logTimeoutProvider")
    public void testCheckLogTimeout(Long lastMessageTimestamp, Long timeout, boolean expectedResult)
    {
        doReturn(mockClients).when(mockPodPushClient).getKubernetesHttpClients();
        doReturn(mockPodPushClient).when(mockPushPodClientFactory).getClient(any());
        podFollower = new PodFollowerImpl(kubeConfig, podConfig, executionConfig, mockPushPodClientFactory);
        when(mockClients.getRequestClient().getLastLogLineTimestamp(any(), any())).thenReturn(lastMessageTimestamp);
        Assert.assertEquals(podFollower.checkLogTimeout(timeout, NAMESPACE, POD_NAME), expectedResult);
    }
}
