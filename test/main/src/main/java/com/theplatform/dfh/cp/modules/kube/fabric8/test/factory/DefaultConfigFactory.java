package com.theplatform.dfh.cp.modules.kube.fabric8.test.factory;

import com.theplatform.dfh.cp.modules.kube.client.config.*;
import com.theplatform.test.modules.resourcereader.ResourceReader;

import java.util.Arrays;
import java.util.Collections;

public class DefaultConfigFactory implements ConfigFactory
{
    private final ResourceReader resourceReader;

    public DefaultConfigFactory(ResourceReader resourceReader)
    {
        this.resourceReader = resourceReader;
    }

    public KubeConfig getDefaultKubeConfig()
    {
        KubeConfig kubeConfig = new KubeConfig();
        kubeConfig.setMasterUrl(resourceReader.getValue("kubernetesMaster"));
        kubeConfig.setNameSpace(resourceReader.getValue("kubernetesNamespace"));
        return kubeConfig;
    }

    public PodConfig getDefaultPodConfig()
    {
        NfsDetails nfsDetails = new NfsDetails();
        nfsDetails.setNfsServerPath(resourceReader.getValue("nfs.serverPath"));
        nfsDetails.setNfsReadOnly(false);
        nfsDetails.setNfsMountPaths(new String[] { resourceReader.getValue("nfs.mountPath") });
        nfsDetails.setNfsServer(resourceReader.getValue("nfs.server"));

        AliveCheckDetails aliveCheckDetails = new AliveCheckDetails();
        aliveCheckDetails.setAliveCheckHost("example.com");
        aliveCheckDetails.setAlivePort(14506);
        aliveCheckDetails.setAlivePath("/management/alive");
        aliveCheckDetails.setAliveCheckLinking(false);

        PodConfig podConfig = new PodConfig().applyDefaults();
        podConfig.setNfsSettings(Collections.singletonList(nfsDetails));
        podConfig.setAliveCheckDetails(aliveCheckDetails);
        podConfig.setMemoryRequestCount("1000m");
        podConfig.setReapCompletedPods(true);
        podConfig.setPodScheduledTimeoutMs(100000L);
        podConfig.setPodStdoutTimeout((long)60 * 1000 * 2);
        podConfig.setCpuMinRequestCount("1000m");
        podConfig.setCpuMaxRequestCount("1000m");
        // PULL ALWAYS FOR THE TESTS
        podConfig.setPullAlways(true);

        return podConfig;
    }
}