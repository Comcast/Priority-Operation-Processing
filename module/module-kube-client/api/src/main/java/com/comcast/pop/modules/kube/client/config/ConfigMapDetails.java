package com.comcast.pop.modules.kube.client.config;

import java.util.List;

/**
 *
 */
public class ConfigMapDetails {
    // The name of the fabric8 ConfigMapVolumeSource
    private String configMapName;
    // The volume name when setting up the config map
    private String volumeName;
    // The mount path to attach the volume created with the above settings
    private String volumeMountPath;

    private List<KeyPathPair> mapKeyPaths;

    public List<KeyPathPair> getMapKeyPaths()
    {
        return mapKeyPaths;
    }

    public ConfigMapDetails setMapKeyPaths(List<KeyPathPair> mapKeyPaths)
    {
        this.mapKeyPaths = mapKeyPaths;
        return this;
    }

    public String getConfigMapName()
    {
        return configMapName;
    }

    public ConfigMapDetails setConfigMapName(String configMapName)
    {
        this.configMapName = configMapName;
        return this;
    }

    public String getVolumeName()
    {
        return volumeName;
    }

    public ConfigMapDetails setVolumeName(String volumeName)
    {
        this.volumeName = volumeName;
        return this;
    }

    public String getVolumeMountPath()
    {
        return volumeMountPath;
    }

    public ConfigMapDetails setVolumeMountPath(String volumeMountPath)
    {
        this.volumeMountPath = volumeMountPath;
        return this;
    }
}