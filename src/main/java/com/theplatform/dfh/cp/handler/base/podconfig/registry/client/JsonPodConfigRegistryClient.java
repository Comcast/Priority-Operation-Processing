package com.theplatform.dfh.cp.handler.base.podconfig.registry.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.podconfig.registry.client.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.handler.base.podconfig.registry.client.util.PropertyCopier;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonPodConfigRegistryClient implements PodConfigRegistryClient {

    public static final String DFH_SERVICE_ACCOUNT_NAME = "ffmpeg-service";
    public static final String DEFAULT_CONFIG_MAP_JSON = "defaultConfigMap.json";
    public static final String BASE_POD_CONFIG_KEY = "basePodConfig";
    public static final PodConfig BASE_POD_CONFIG;

    static {
        BASE_POD_CONFIG = new PodConfig();

        ConfigMapDetails configMapDetails = new ConfigMapDetails()
                .setMapKey("external-properties")
                .setMapPath("external.properties")
                .setVolumeName("config-volume")
                .setVolumeMountPath("/config");

        BASE_POD_CONFIG.setServiceAccountName(DFH_SERVICE_ACCOUNT_NAME)
                .setEndOfLogIdentifier(BaseHandlerEntryPoint.DFH_POD_TERMINATION_STRING)
                .setConfigMapDetails(configMapDetails);
    }

    private String path;
    private Map<String, PodConfig> podConfigMap = new HashMap<>();

    public JsonPodConfigRegistryClient() throws PodConfigRegistryClientException{
        loadJsonMap();
    }

    public JsonPodConfigRegistryClient(String path) throws PodConfigRegistryClientException {
        this.path = path;
        loadJsonMap();
    }

    private void loadJsonMap() throws PodConfigRegistryClientException {
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
                throw new PodConfigRegistryClientException("Could not find JSON ConfigMap: " + this.path, ex);
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

    @Override
    public PodConfig getPodConfig(String configMapName) throws PodConfigRegistryClientException {
        if (!podConfigMap.containsKey(configMapName)) {
            throw new PodConfigRegistryClientException("Could not find PodConfig with name: " + configMapName);
        }

        return podConfigMap.get(configMapName);
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