package com.theplatform.dfh.cp.modules.kube.client.config;

/**
 *
 */
public class ConfigMapDetails {
    // 	The key value on a fabric8 KeyToPath when setting up a configmap for the pod
    private String mapKey;
    // The map value on a fabric8 KeyToPath when setting up a configmap for the pod
    private String mapPath;
    // The name of the fabric8 ConfigMapVolumeSource
    private String volumeSourceName;
    // The volume name when setting up the config map
    private String volumeName;
    // The mount path to attach the volume created with the above settings
    private String volumeMountPath;

    public String getMapKey()
    {
        return mapKey;
    }

    public ConfigMapDetails setMapKey(String mapKey)
    {
        this.mapKey = mapKey;
        return this;
    }

    public String getMapPath()
    {
        return mapPath;
    }

    public ConfigMapDetails setMapPath(String mapPath)
    {
        this.mapPath = mapPath;
        return this;
    }

    public String getVolumeSourceName()
    {
        return volumeSourceName;
    }

    public ConfigMapDetails setVolumeSourceName(String volumeSourceName)
    {
        this.volumeSourceName = volumeSourceName;
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