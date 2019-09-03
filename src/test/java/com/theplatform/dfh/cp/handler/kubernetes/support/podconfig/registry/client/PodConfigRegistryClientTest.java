package com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.registry.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.JsonPodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.KeyPathPair;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class PodConfigRegistryClientTest {

    final String POD_REGISTRY_FILE = "testConfigMap.json";
    final String POD_REGISTRY_FILE_WITH_BASE_CONFIG_MAP_DETAILS = "testConfigMap_BaseConfigMap.json";
    final String POD_REGISTRY_PATH =  getClass().getClassLoader().getResource(POD_REGISTRY_FILE).getPath();
    private JsonPodConfigRegistryClient podConfigRegistryClient;

    @BeforeMethod
    public void setup()
    {
        podConfigRegistryClient = createJsonRegistryClient(POD_REGISTRY_FILE);
    }

    @Test
    void testDefaultJsonClient() throws PodConfigRegistryClientException
    {
        JsonPodConfigRegistryClient client = new JsonPodConfigRegistryClient();
        PodConfig pConfig = client.getPodConfig("encode");

        Assert.assertEquals(pConfig.getImageName(), "docker-lab.repo.theplatform.com/fheff:1.0.0");
    }

    @Test
    void testBaseOverlayOnSample() throws PodConfigRegistryClientException, IOException
    {
        final String VOLUME_NAME = "config-volume-ex";
        final String VOLUME_MOUNT_PATH = "/configex";

        // see json file for sample's settings
        final KeyPathPair KeyPathZero = new KeyPathPair("1", "2");
        final KeyPathPair KeyPathOne = new KeyPathPair("3", "4");

        // base json podConfig
        PodConfig baseJsonPodConfig = getBaseJsonPodConfig(POD_REGISTRY_PATH);

        PodConfig pConfig = podConfigRegistryClient.getPodConfig("sample");

        // validate the "base" fields we start out with
        Assert.assertEquals(pConfig.getMemoryRequestCount(), baseJsonPodConfig.getMemoryRequestCount());
        Assert.assertEquals(pConfig.getCpuMinRequestCount(), baseJsonPodConfig.getCpuMinRequestCount());
        Assert.assertEquals(pConfig.getCpuMaxRequestCount(), baseJsonPodConfig.getCpuMaxRequestCount());
        Assert.assertEquals(pConfig.getPodScheduledTimeoutMs(), baseJsonPodConfig.getPodScheduledTimeoutMs());
        Assert.assertEquals(pConfig.getReapCompletedPods(), baseJsonPodConfig.getReapCompletedPods());
        Assert.assertEquals(pConfig.getPullAlways(), baseJsonPodConfig.getPullAlways());
        Assert.assertEquals(pConfig.getEndOfLogIdentifier(), BaseHandlerEntryPoint.DFH_POD_TERMINATION_STRING);
        Assert.assertNotNull(pConfig.getConfigMapSettings());
        Assert.assertEquals(pConfig.getConfigMapSettings().size(), 1);
        ConfigMapDetails configMapDetails = pConfig.getConfigMapSettings().get(0);
        Assert.assertEquals(configMapDetails.getVolumeName(), VOLUME_NAME);
        Assert.assertEquals(configMapDetails.getVolumeMountPath(), VOLUME_MOUNT_PATH);

        // validate the json registry values we overlay
        Assert.assertEquals(pConfig.getImageName(), "docker-lab.repo.theplatform.com/fhsamp:1.0.0");
        Assert.assertEquals(pConfig.getNamePrefix(), "dfh-samp");

        // validate final overlay from the sample entry
        Assert.assertNotNull(configMapDetails.getMapKeyPaths());
        Assert.assertEquals(configMapDetails.getMapKeyPaths().size(), 2);
        verifyKeyPathMatches(configMapDetails.getMapKeyPaths().get(0), KeyPathZero);
        verifyKeyPathMatches(configMapDetails.getMapKeyPaths().get(1), KeyPathOne);
    }

    @DataProvider Object[][] configMapDetails()
    {
        return new Object[][]
            {
                {"analysis"},
                {"encode"},
                {"thumbnail"},
                {"package"},
            };
    }

    @Test(dataProvider = "configMapDetails")
    public void testConfigMapDetailsCopied(String type) throws PodConfigRegistryClientException
    {
        // see json file for the base config settings
        final KeyPathPair KeyPathZero = new KeyPathPair("external-properties", "external.properties");
        final KeyPathPair KeyPathOne = new KeyPathPair("env-properties", "env.properties");

        PodConfig pConfig = podConfigRegistryClient.getPodConfig(type);

        Assert.assertNotNull(pConfig.getConfigMapSettings());
        Assert.assertEquals(pConfig.getConfigMapSettings().size(), 1);
        ConfigMapDetails configMapDetails = pConfig.getConfigMapSettings().get(0);
        Assert.assertEquals(configMapDetails.getVolumeName(), JsonPodConfigRegistryClient.DEFAULT_VOLUME_NAME);
        Assert.assertEquals(configMapDetails.getVolumeMountPath(), JsonPodConfigRegistryClient.DEFAULT_VOLUME_MOUNT_PATH);

        // validate the nested fields are set
        Assert.assertNotNull(configMapDetails.getMapKeyPaths());
        Assert.assertEquals(configMapDetails.getMapKeyPaths().size(), 2);
        verifyKeyPathMatches(configMapDetails.getMapKeyPaths().get(0), KeyPathZero);
        verifyKeyPathMatches(configMapDetails.getMapKeyPaths().get(1), KeyPathOne);
    }

    // cheapo comparison method
    private void verifyKeyPathMatches(KeyPathPair pair, KeyPathPair otherPair)
    {
        Assert.assertEquals(pair.getKey(), otherPair.getKey());
        Assert.assertEquals(pair.getPath(), otherPair.getPath());
    }


    @Test
    void testBuiltInFailover() throws PodConfigRegistryClientException
    {
        JsonPodConfigRegistryClient client = new JsonPodConfigRegistryClient("/non/existent/registry.json");
        PodConfig pConfig = client.getPodConfig("encode");

        Assert.assertEquals(pConfig.getImageName(), "docker-lab.repo.theplatform.com/fheff:1.0.0");
    }

    @Test
    void testPodConfigIsCloned() throws PodConfigRegistryClientException
    {
        final int MAP_KEY_PATHS_SIZE = 2;
        PodConfig podConfig = podConfigRegistryClient.getPodConfig("sample");
        Assert.assertNotNull(podConfig.getConfigMapSettings());
        Assert.assertEquals(podConfig.getConfigMapSettings().size(), 1);
        ConfigMapDetails configMapDetails = podConfig.getConfigMapSettings().get(0);
        Assert.assertEquals(configMapDetails.getMapKeyPaths().size(), MAP_KEY_PATHS_SIZE, "This test requires the ConfigMapDetails.MapKeyPaths size to be " + MAP_KEY_PATHS_SIZE);
        configMapDetails.getMapKeyPaths().add(new KeyPathPair("key", "path"));

        PodConfig clonedConfig = podConfigRegistryClient.getPodConfig("sample");
        Assert.assertNotNull(clonedConfig.getConfigMapSettings());
        Assert.assertEquals(clonedConfig.getConfigMapSettings().size(), 1);
        ConfigMapDetails clonedConfigmapDetails = clonedConfig.getConfigMapSettings().get(0);
        Assert.assertEquals(clonedConfigmapDetails.getMapKeyPaths().size(), MAP_KEY_PATHS_SIZE, "Cloned configuration is using the same references as the original!");
    }

    @Test
    void testPodConfigBaseMapDetails() throws Exception
    {
        JsonPodConfigRegistryClient client = createJsonRegistryClient(POD_REGISTRY_FILE_WITH_BASE_CONFIG_MAP_DETAILS);
        PodConfig podConfig = client.getPodConfig("analysis");

        Assert.assertNotNull(podConfig.getConfigMapSettings());
        Assert.assertEquals(podConfig.getConfigMapSettings().size(), 1);
        ConfigMapDetails configMapDetails = podConfig.getConfigMapSettings().get(0);
        Assert.assertEquals(configMapDetails.getVolumeName(), "config-volume-ex");
        Assert.assertEquals(configMapDetails.getVolumeMountPath(), "/configex");

        // validate the nested fields are set
        Assert.assertNotNull(configMapDetails.getMapKeyPaths());
        Assert.assertEquals(configMapDetails.getMapKeyPaths().size(), 1);
        verifyKeyPathMatches(configMapDetails.getMapKeyPaths().get(0),
            new KeyPathPair("X", "Y"));
    }

    private PodConfig getBaseJsonPodConfig(String path) throws IOException
    {
        String json = JsonPodConfigRegistryClient.readFromInputStream(new FileInputStream(path));

        JsonNode node = new ObjectMapper().readTree(json);

        return new ObjectMapper().readValue(node.get(JsonPodConfigRegistryClient.BASE_POD_CONFIG_KEY).toString(), PodConfig.class);
    }

    private JsonPodConfigRegistryClient createJsonRegistryClient(String resourceFileName)
    {
        String path = getClass().getClassLoader().getResource(resourceFileName).getPath();

        return new JsonPodConfigRegistryClient(path);
    }
}
