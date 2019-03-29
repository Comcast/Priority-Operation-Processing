package com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.util.PropertyCopier;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.KeyPathPair;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class JsonPodConfigRegistryClient implements PodConfigRegistryClient {
    private static Logger logger = LoggerFactory.getLogger(JsonPodConfigRegistryClient.class);

    public static final String DFH_SERVICE_ACCOUNT_NAME = "ffmpeg-service";
    public static final String DEFAULT_CONFIG_MAP_JSON = "defaultConfigMap.json";
    public static final String BASE_POD_CONFIG_KEY = "basePodConfig";
    public static final PodConfig BASE_POD_CONFIG;

    static {
        BASE_POD_CONFIG = new PodConfig().applyDefaults();

        ConfigMapDetails configMapDetails = new ConfigMapDetails()
                .setVolumeName("config-volume")
                .setVolumeMountPath("/config");

        List<KeyPathPair> keyPaths = new LinkedList<>();
        keyPaths.add(new KeyPathPair("external-properties", "external.properties"));

        configMapDetails.setMapKeyPaths(keyPaths);

        BASE_POD_CONFIG.setServiceAccountName(DFH_SERVICE_ACCOUNT_NAME)
                .setEndOfLogIdentifier(BaseHandlerEntryPoint.DFH_POD_TERMINATION_STRING)
                .setConfigMapDetails(configMapDetails);
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
            PodConfig podConfig = new PodConfig();
            try
            {
                PropertyCopier.copyProperties(podConfig, originalConfig);
            }
            catch (IllegalAccessException | InvocationTargetException ex) {
                throw new PodConfigRegistryClientException("Unable to map properties when duplicating PodConfig from registry: " + configMapName, ex);
            }
            return podConfig;
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

        // get json
        try
        {
            String json = readFromInputStream(stream);
            JsonNode node = new ObjectMapper().readTree(json);

            // ensure BASE_POD_CONFIG_KEY exists in the json file
            if (!node.has(BASE_POD_CONFIG_KEY)) {
                throw new PodConfigRegistryClientException("The specified JSON file is missing: "  + DEFAULT_CONFIG_MAP_JSON);
            }

            // grab the basePodConfig json entry
            PodConfig baseJsonPodConfig = new ObjectMapper().readValue(node.get(BASE_POD_CONFIG_KEY).toString(), PodConfig.class);

            // iterate over each handler-type key [sample, encode, analysis, etc..]
            Iterator<String> fieldNamesIter = node.fieldNames();
            while (fieldNamesIter.hasNext()) {
                String handlerType = fieldNamesIter.next();

                // skip the base pod config entry
                if (handlerType.equalsIgnoreCase(BASE_POD_CONFIG_KEY)) {
                    continue;
                }

                JsonNode handlerNode = node.get(handlerType);
                String configMapName = handlerNode.get("configMapName").textValue();
                JsonNode podConfigNode = handlerNode.get("podConfig");

                // merge this class's basePodConfig with the JSON basePodConfig
                PodConfig basePodConfig = generateBasePodConfig(configMapName, baseJsonPodConfig);

                // get the partial (or full) podConfig from the JSON registry entry
                PodConfig loadedPodConfig = new ObjectMapper().readValue(podConfigNode.toString(), PodConfig.class);

                // overlay the loadedPodConfig on top of the basePodConfig - this could overwrite some or ALL fields
                PropertyCopier.copyProperties(basePodConfig, loadedPodConfig);

                // add to map for lookup
                podConfigMap.put(handlerType, basePodConfig);
            }

            loaded = true;
        }
        catch(IOException ex) {
            throw new PodConfigRegistryClientException("There was a problem trying to read from registry JSON file: " + this.path, ex);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new PodConfigRegistryClientException("There was a problem trying to load registry JSON file: " + this.path, ex);
        }
    }

    private PodConfig generateBasePodConfig(String configMapName, PodConfig baseJsonPodConfig) throws InvocationTargetException, IllegalAccessException {
        PodConfig result = new PodConfig();

        // first overlay the base we have here
        PropertyCopier.copyProperties(result, BASE_POD_CONFIG);
        result.getConfigMapDetails().setConfigMapName(configMapName);

        // next overlay the JSON base pod config
        PropertyCopier.copyProperties(result, baseJsonPodConfig);

        return result;
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

    public String getPath() {
        return path;
    }
}