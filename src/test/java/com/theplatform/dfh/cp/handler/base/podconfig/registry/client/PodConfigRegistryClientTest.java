package com.theplatform.dfh.cp.handler.base.podconfig.registry.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.podconfig.registry.client.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class PodConfigRegistryClientTest {
    @Test
    void testDefaultJsonClient() throws PodConfigRegistryClientException {
        JsonPodConfigRegistryClient client = new JsonPodConfigRegistryClient();
        PodConfig pConfig = client.getPodConfig("encode");

        Assert.assertEquals(pConfig.getImageName(), "docker-lab.repo.theplatform.com/fheff:1.0.0");
    }

    @Test
    void testBaseOverlayOnSample() throws PodConfigRegistryClientException, IOException {
        String path = getClass().getClassLoader().getResource("testConfigMap.json").getPath();

        // base json podConfig
        PodConfig baseJsonPodConfig = getBaseJsonPodConfig(path);

        JsonPodConfigRegistryClient client = new JsonPodConfigRegistryClient(path);
        PodConfig pConfig = client.getPodConfig("sample");

        // validate the "base" fields we start out with
        Assert.assertEquals(pConfig.getMemoryRequestCount(), baseJsonPodConfig.getMemoryRequestCount());
        Assert.assertEquals(pConfig.getCpuMinRequestCount(), baseJsonPodConfig.getCpuMinRequestCount());
        Assert.assertEquals(pConfig.getCpuMaxRequestCount(), baseJsonPodConfig.getCpuMaxRequestCount());
        Assert.assertEquals(pConfig.getPodScheduledTimeoutMs(), baseJsonPodConfig.getPodScheduledTimeoutMs());
        Assert.assertEquals(pConfig.getReapCompletedPods(), baseJsonPodConfig.getReapCompletedPods());
        Assert.assertEquals(pConfig.getPullAlways(), baseJsonPodConfig.getPullAlways());
        Assert.assertEquals(pConfig.getEndOfLogIdentifier(), BaseHandlerEntryPoint.DFH_POD_TERMINATION_STRING);

        // validate the json registry values we overlay
        Assert.assertEquals(pConfig.getImageName(), "docker-lab.repo.theplatform.com/fhsamp:1.0.0");
        Assert.assertEquals(pConfig.getNamePrefix(), "dfh-samp");
    }

    private PodConfig getBaseJsonPodConfig(String path) throws IOException {
        String json = JsonPodConfigRegistryClient.readFromInputStream(new FileInputStream(path));

        JsonNode node = new ObjectMapper().readTree(json);

        return new ObjectMapper().readValue(node.get(JsonPodConfigRegistryClient.BASE_POD_CONFIG_KEY).toString(), PodConfig.class);
    }
}
