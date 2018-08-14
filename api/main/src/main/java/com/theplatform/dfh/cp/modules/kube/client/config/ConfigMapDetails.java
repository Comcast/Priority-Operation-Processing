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

    public void setMapKey(String mapKey)
    {
        this.mapKey = mapKey;
    }

    public String getMapPath()
    {
        return mapPath;
    }

    public void setMapPath(String mapPath)
    {
        this.mapPath = mapPath;
    }

    public String getVolumeSourceName()
    {
        return volumeSourceName;
    }

    public void setVolumeSourceName(String volumeSourceName)
    {
        this.volumeSourceName = volumeSourceName;
    }

    public String getVolumeName()
    {
        return volumeName;
    }

    public void setVolumeName(String volumeName)
    {
        this.volumeName = volumeName;
    }

    public String getVolumeMountPath()
    {
        return volumeMountPath;
    }

    public void setVolumeMountPath(String volumeMountPath)
    {
        this.volumeMountPath = volumeMountPath;
    }
}