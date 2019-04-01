package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.RetryablePodResource;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.SocketTimeoutException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RetryablePodResourceTest
{
    private RetryablePodResource retryablePodResource;
    private PodResource<Pod, DoneablePod> mockPodResource;

    @BeforeMethod
    public void setup()
    {
        mockPodResource = (PodResource<Pod, DoneablePod>)mock(PodResource.class);
        retryablePodResource = new RetryablePodResource(mockPodResource);
    }

    @Test
    public void testGetRetries()
    {
        final int ATTEMPTS = 5;
        final int DELAY_SECONDS = 0;
        retryablePodResource.setAttemptCount(ATTEMPTS);
        retryablePodResource.setDelaySeconds(DELAY_SECONDS);

        doThrow(new KubernetesClientException("", new SocketTimeoutException())).when(mockPodResource).get();
        try
        {
            retryablePodResource.get();
            Assert.fail("The exception should have been thrown!");
        }
        catch(KubernetesClientException e)
        {
            // this is as expected
        }
        verify(mockPodResource, times(ATTEMPTS)).get();
    }
}
