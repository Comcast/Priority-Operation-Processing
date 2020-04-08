package com.comcast.pop.handler.kubernetes.support.podconfig.client.registry;

import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comast.pop.handler.base.BaseHandlerEntryPoint;
import com.comcast.pop.modules.kube.client.config.ConfigMapDetails;
import com.comcast.pop.modules.kube.client.config.KeyPathPair;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class JsonPodConfigRegistryClient implements PodConfigRegistryClient {
    private static Logger logger = LoggerFactory.getLogger(JsonPodConfigRegistryClient.class);

    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static final String CONFIG_MAP_NAME_NODE = "configMapName";
    public static final String POP_SERVICE_ACCOUNT_NAME = "pop-service";
    public static final String DEFAULT_CONFIG_MAP_JSON = "defaultConfigMap.json";
    public static final String BASE_POD_CONFIG_KEY = "basePodConfig";
    public static final String DEFAULT_VOLUME_NAME = "config-volume";
    public static final String DEFAULT_VOLUME_MOUNT_PATH = "/app/config";

    public static final JsonNode CORE_POD_CONFIG_NODE;

    static {
        PodConfig corePodConfig = new PodConfig().applyDefaults();

        ConfigMapDetails configMapDetails = new ConfigMapDetails()
                .setVolumeName(DEFAULT_VOLUME_NAME)
                .setVolumeMountPath(DEFAULT_VOLUME_MOUNT_PATH);

        List<KeyPathPair> keyPaths = new LinkedList<>();
        keyPaths.add(new KeyPathPair("external-properties", "external.properties"));
        keyPaths.add(new KeyPathPair("env-properties", "env.properties"));

        configMapDetails.setMapKeyPaths(keyPaths);

        corePodConfig.setServiceAccountName(POP_SERVICE_ACCOUNT_NAME)
                .setEndOfLogIdentifier(BaseHandlerEntryPoint.POD_TERMINATION_STRING)
                .setConfigMapSettings(Collections.singletonList(configMapDetails));

        CORE_POD_CONFIG_NODE = objectMapper.valueToTree(corePodConfig);
    }

    private String path;
    private Map<String, PodConfig> podConfigMap = new HashMap<>();
    private boolean loaded = false;

    public JsonPodConfigRegistryClient() {
        logger.debug("Loading JSON map from built-in map.");
    }

    public JsonPodConfigRegistryClient(String path) {
        this.path = path;
        logger.debug("Loading JSON map from: " + this.path);
    }

    @Override
    public PodConfig getPodConfig(String configMapName) throws PodConfigRegistryClientException
    {
        logger.debug("getPodConfig called for `" + configMapName + "`");
        // did we load the json registry yet?
        if (!loaded) {
            loadJsonMap();
        }

        if (!podConfigMap.containsKey(configMapName)) {
            throw new PodConfigRegistryClientException("Could not find PodConfig with name: " + configMapName);
        }

        PodConfig originalConfig = podConfigMap.get(configMapName);
        if(originalConfig != null)
        {
            try
            {
                return objectMapper.readValue(objectMapper.writeValueAsString(originalConfig), PodConfig.class);
            }
            catch (IOException ex) {
                throw new PodConfigRegistryClientException("Unable to map properties when duplicating PodConfig from registry: " + configMapName, ex);
            }
        }
        return null;
    }

    private void loadJsonMap() throws PodConfigRegistryClientException {
        logger.debug("loadJsonMap called");
        InputStream stream;

        // get stream
        if (this.path == null || this.path.isEmpty()) {
            stream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_MAP_JSON);
        }
        else {
            try
            {
                stream = new FileInputStream(this.path);
            }
            catch(FileNotFoundException ex)
            {
                // file was not found then we default to the internal map
                logger.warn("Could not load external registry map @ " + this.path);
                logger.warn("Loading built-in registry..");
                stream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_MAP_JSON);
            }
        }

        try
        {
            String json = readFromInputStream(stream);
            JsonNode node = new ObjectMapper().readTree(json);

            // ensure BASE_POD_CONFIG_KEY exists in the json file
            if (!node.has(BASE_POD_CONFIG_KEY)) {
                throw new PodConfigRegistryClientException("The specified JSON file is missing: "  + DEFAULT_CONFIG_MAP_JSON);
            }

            // grab the basePodConfig json entry
            JsonNode basePodConfigNode = node.get(BASE_POD_CONFIG_KEY);

            // iterate over each handler-type key [sample, encode, analysis, etc..]
            Iterator<String> fieldNamesIter = node.fieldNames();
            while (fieldNamesIter.hasNext()) {
                String handlerType = fieldNamesIter.next();

                // skip the base pod config entry
                if (handlerType.equalsIgnoreCase(BASE_POD_CONFIG_KEY)) {
                    continue;
                }

                JsonNode handlerNode = node.get(handlerType);
                // NOTE: This is the one dynamic field that is changed in the ConfigMapDetails nested within the PodConfig (convenience)
                String configMapName = retrieveNodeText(handlerNode, CONFIG_MAP_NAME_NODE, null);
                JsonNode podConfigNode = handlerNode.get("podConfig");

                PodConfig combinedConfig = combineConfig(CORE_POD_CONFIG_NODE, basePodConfigNode, podConfigNode);
                // set the configMap name on the first entry if there is one and a config map name was specified
                if(configMapName != null && combinedConfig.getConfigMapSettings() != null && combinedConfig.getConfigMapSettings().size() > 0)
                    combinedConfig.getConfigMapSettings().get(0).setConfigMapName(configMapName);

                podConfigMap.put(handlerType, combinedConfig);
            }

            loaded = true;
        }
        catch(IOException ex) {
            throw new PodConfigRegistryClientException("There was a problem trying to read from registry JSON file: " + this.path, ex);
        }
    }

    protected String retrieveNodeText(JsonNode node, String nodeName, String defaultValue)
    {
        JsonNode retrievedNode = node.get(nodeName);
        return retrievedNode.isMissingNode() ? defaultValue : retrievedNode.textValue();
    }

    /**
     * Combines the configs as layers via ObjectMapper (This does not merge sub objects, only overwrite!)
     * @param podConfigLayers The ordered layers of PodConfigs to combine
     * @return PodConfig with all the layers combined
     * @throws IOException Thrown if there is an issue reading the json node
     */
    private PodConfig combineConfig(JsonNode... podConfigLayers) throws IOException
    {
        PodConfig podConfig = new PodConfig();
        for(JsonNode nodeLayer : podConfigLayers)
        {
            objectMapper.readerForUpdating(podConfig).readValue(nodeLayer);
        }
        return podConfig;
    }

    public static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}