package com.theplatform.dfh.cp.modules.kube.fabric8.client;

import com.theplatform.dfh.cp.modules.kube.client.config.*;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.zjsonpatch.internal.guava.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 *
 */
public class Fabric8Helper
{
    public static final int DEFAULT_WATCH_RECONNECT_LIMIT = 10;
    public static final String EMPTY_DIR_LOG_NAME = "log";
    public static final String APP_DUMPS = "/app/dumps";
    public static final String EXTERNAL_ID = "EXTERNAL_ID";
    public static final int MAX_CONCURRENT_REQUESTS_PER_HOST = 20;
    private static final String APP_KEY = "app";
    public static final String MY_POD_NAME = "MY_POD_NAME";
    public static final String EXTERNAL_GROUP_ID = "EXTERNAL_GROUP_ID";
    public static final String KUBE_API_VERSION = "v1";
    public static final String RESTART_POLICY = "Never";

    private static Logger logger = LoggerFactory.getLogger(Fabric8Helper.class);

    public static String generateUUID()
    {
        return UUID.randomUUID().toString();
    }

    public static Config getFabric8Config(KubeConfig kubeConfig)
    {
        ConfigBuilder configBuilder = new ConfigBuilder();
        if (kubeConfig.getMasterUrl() != null)
        {
            configBuilder.withMasterUrl(kubeConfig.getMasterUrl());
        }
        if (kubeConfig.getNameSpace() != null)
        {
            configBuilder.withNamespace(kubeConfig.getNameSpace());
        }
        if (kubeConfig.getCertificateAuthorityPath() != null)
        {
            configBuilder.withCaCertFile(kubeConfig.getCertificateAuthorityPath());
        }
        if (kubeConfig.getCertificatePath() != null)
        {
            configBuilder.withClientCertFile(kubeConfig.getCertificatePath());
        }
        if (kubeConfig.getKeyPath() != null)
        {
            configBuilder.withClientKeyFile(kubeConfig.getKeyPath());
        }
        if (kubeConfig.getCaCertData() != null)
        {
            configBuilder.withCaCertData(kubeConfig.getCaCertData());
        }
        if (kubeConfig.getOauthToken() != null)
        {
            configBuilder.withOauthToken(kubeConfig.getOauthToken());
        }

        if(kubeConfig.getWatchReconnectLimit() != null)
        {
            configBuilder.withWatchReconnectLimit(kubeConfig.getWatchReconnectLimit());
        }
        else
        {
            configBuilder.withWatchReconnectLimit(DEFAULT_WATCH_RECONNECT_LIMIT);
        }

        if(kubeConfig.getWatchReconnectInterval() != null)
        {
            configBuilder.withWatchReconnectInterval(kubeConfig.getWatchReconnectInterval());
        }

        configBuilder.withMaxConcurrentRequestsPerHost(MAX_CONCURRENT_REQUESTS_PER_HOST);
        return configBuilder.build();
    }

    // TODO split this into ContainerSpec and PodSpec builders 
    public static Pod getPodSpec(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        Map<String, String> labels = new HashMap<>();
        String label = podConfig.getNamePrefix();
        labels.put(APP_KEY, label);
        labels.put(EXTERNAL_ID, executionConfig.getExternalId());
        labels.put(EXTERNAL_GROUP_ID, executionConfig.getExternalGroupId());

        List<ConfigMapDetails> configMapSettings = podConfig.getConfigMapSettings() != null ? podConfig.getConfigMapSettings() : new ArrayList<>();

        final PodSpecFluent.ContainersNested<PodFluent.SpecNested<PodBuilder>> containerSpec = new PodBuilder()
            .withApiVersion(KUBE_API_VERSION)
            .withKind("Pod")
            .withNewMetadata()
            .withName(executionConfig.getName())
            .withNamespace(kubeConfig.getNameSpace())
            .withLabels(labels)
            .endMetadata()
            .withNewSpec()
            .withContainers()
            .addNewContainer()
            .withName(label + generateUUID())
            .withImage(podConfig.getImageName())
            .withArgs(podConfig.getArguments())
            .withVolumeMounts();

        // IMPORTANT: this must be within `addNewContainer()`
        // if isDockerPrivileged we need to set a SecurityContext at the Image level
        if (podConfig.getDockerPrivileged())
        {
            SecurityContext secContext = new SecurityContext();
            secContext.setPrivileged(true);

            containerSpec.withSecurityContext(secContext);
        }

        if (podConfig.hasEnvVars())
        {
            Map<String, String> envVars = podConfig.getEnvVars();
            envVars.forEach((k, v) -> containerSpec.addNewEnv().withName(k).withValue(v).endEnv());
        }
        if (executionConfig.hasEnvVars())
        {
            Map<String, String> envVars = executionConfig.getEnvVars();
            envVars.forEach((k, v) -> containerSpec.addNewEnv().withName(k).withValue(v).endEnv());
        }

        containerSpec.addNewEnv().withName(MY_POD_NAME).withNewValueFrom().withNewFieldRef().withFieldPath("metadata.name").endFieldRef().endValueFrom().endEnv();

        if (podConfig.getDefaultEmptyDirLogging())
        {
            containerSpec.addNewVolumeMount().withName(EMPTY_DIR_LOG_NAME).withMountPath(APP_DUMPS).endVolumeMount();
        }

        configMapSettings.forEach(configMapDetails ->
            containerSpec.addNewVolumeMount()
                .withName(configMapDetails.getVolumeName())
                .withMountPath(configMapDetails.getVolumeMountPath())
                .endVolumeMount()
        );

        if (podConfig.getPullAlways())
        {
            containerSpec.withImagePullPolicy("Always");
        }

        // map of nfsdetails to all the different volume names for each volumePath specified
        Map<NfsDetails, List<String>> nfsDetailsVolumeMap = new HashMap<>();
        // add the nfs settings for the individual containers
        if (podConfig.getNfsSettings() != null)
        {
            podConfig.getNfsSettings().forEach(nfsDetails -> {

                if(nfsDetails.getNfsMountPaths() != null)
                {
                    List<String> volumeNames = new LinkedList<>();
                    for (String volumePath : nfsDetails.getNfsMountPaths())
                    {
                        String volumeName = generateUUID();
                        volumeNames.add(volumeName);
                        containerSpec
                            .addNewVolumeMount()
                            .withName(volumeName)
                            .withMountPath(volumePath)
                            .endVolumeMount();
                    }
                    nfsDetailsVolumeMap.put(nfsDetails, volumeNames);
                }
            });
        }

        if (executionConfig.getCpuRequestModulator() == null)
            throw new IllegalArgumentException("Must provide a CpuRequestModulator on ExecutionConfig.");

        containerSpec
            .withNewResources()
            .addToRequests("cpu", new Quantity(executionConfig.getCpuRequestModulator().getCpuRequest()))
            .addToRequests("memory", new Quantity(podConfig.getMemoryRequestCount()))
            .addToLimits("cpu", new Quantity(executionConfig.getCpuRequestModulator().getCpuLimit()))
            .endResources();

        AliveCheckDetails aliveCheckDetails = podConfig.getAliveCheckDetails();
        if (aliveCheckDetails != null && aliveCheckDetails.getAliveCheckLinking())
        {
            logger.info("Adding alive check linking");
            try
            {
                String host = Strings.isNullOrEmpty(aliveCheckDetails.getAliveCheckHost())
                              ? System.getenv("MY_POD_IP")
                              : aliveCheckDetails.getAliveCheckHost();
                URL aliveUrl = new URL("http", host, aliveCheckDetails.getAlivePort(), aliveCheckDetails.getAlivePath());
                LinkedList<String> list = new LinkedList<>();
                list.add("/bin/sh");
                list.add("-c");
                list.add("curl -sSf -m 3 -o /dev/null " + aliveUrl.toString());
                containerSpec
                    .withNewLivenessProbe()
                    .withExec(new ExecActionBuilder(true).addAllToCommand(list).build())
                    .withPeriodSeconds(aliveCheckDetails.getAliveCheckInterval())
                    .withFailureThreshold(aliveCheckDetails.getAliveCheckFailureThreshold())
                    .withTimeoutSeconds(5)
                    .withSuccessThreshold(1)
                    .endLivenessProbe();
            }
            catch (Exception e)
            {
                logger.info("Could not get ip of host to link child pods");
                throw new KubernetesClientException("Could not get ip of host to link child pods.", e);
            }
        }

        PodFluent.SpecNested<PodBuilder> podSpec =
            containerSpec
                .endContainer()
                .withVolumes();

        nfsDetailsVolumeMap.entrySet().forEach(kvp ->
        {
            NfsDetails nfsDetails = kvp.getKey();
            NFSVolumeSource nfsVolumeSource = new NFSVolumeSource(nfsDetails.getNfsServerPath(), nfsDetails.getNfsReadOnly(), nfsDetails.getNfsServer());
            kvp.getValue().forEach(volumeName ->
                podSpec
                    .addNewVolume()
                    .withName(volumeName)
                    .withNfs(nfsVolumeSource)
                    .endVolume()
            );
        });

        if (podConfig.hasServiceAccountName())
        {
            podSpec.withServiceAccount(podConfig.getServiceAccountName());
        }

        configMapSettings.forEach(configMapDetails -> {
            ConfigMapVolumeSource source = new ConfigMapVolumeSource();
            List<KeyToPath> items = new LinkedList<>();

            // if multiple keyPaths are defined via mapKeyPaths list
            if (configMapDetails.getMapKeyPaths() != null && configMapDetails.getMapKeyPaths().size() > 0)
            {
                for (KeyPathPair pair : configMapDetails.getMapKeyPaths())
                {
                    KeyToPath keyToPath = new KeyToPath();
                    keyToPath.setKey(pair.getKey());
                    keyToPath.setPath(pair.getPath());
                    items.add(keyToPath);
                }
            }

            source.setItems(items);
            source.setName(configMapDetails.getConfigMapName());
            podSpec.addNewVolume().withName(configMapDetails.getVolumeName()).withConfigMap(source)
                    .endVolume();
        });

        if (podConfig.getDefaultEmptyDirLogging())
        {
            podSpec.addNewVolume().withName(EMPTY_DIR_LOG_NAME).withEmptyDir(new EmptyDirVolumeSource()).endVolume();
        }

        if (podConfig.getUseTaintedNodes())
        {
            if(StringUtils.isBlank(podConfig.getTaintedNodeSelector()) || StringUtils.isBlank(podConfig.getTaintedNodeToleration()))
                throw new KubernetesClientException("Both the taintedNodeSelector and taintedNodeToleration must be specified if tainted node use is enabled.");

            NodeSelectorRequirement nodeSelectorRequirement = new NodeSelectorRequirement("dedicated", "In",
                Collections.singletonList(podConfig.getTaintedNodeSelector()));
            NodeAffinity nodeAffinity = new NodeAffinityBuilder()
                .withPreferredDuringSchedulingIgnoredDuringExecution(new PreferredSchedulingTermBuilder()
                    .withWeight(40)
                    .withPreference(new NodeSelectorTermBuilder()
                        .withMatchExpressions(nodeSelectorRequirement)
                        .build())
                    .build())
                .build();
            Affinity affinity = new AffinityBuilder().withNodeAffinity(nodeAffinity).build();
            podSpec.withAffinity(affinity);
            Toleration toleration = new Toleration("NoSchedule", "dedicated", "Equal", null, podConfig.getTaintedNodeToleration());
            podSpec.withTolerations(toleration);
        }

        if (kubeConfig.getZone() != null && kubeConfig.getZone().length() > 0)
        {
            logger.debug("Adding zone: " + kubeConfig.getZone());
            podSpec.addToNodeSelector(kubeConfig.getZone(), "true");
        }
        else
        {
            logger.debug("No zone to add");
        }

        return podSpec
            .withRestartPolicy(RESTART_POLICY)
            .endSpec()
            .build();
    }
}
