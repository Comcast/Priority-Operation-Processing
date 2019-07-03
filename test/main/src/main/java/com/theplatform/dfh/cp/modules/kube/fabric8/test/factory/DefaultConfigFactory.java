package com.theplatform.dfh.cp.modules.kube.fabric8.test.factory;

import com.theplatform.dfh.cp.modules.kube.client.config.*;

public class DefaultConfigFactory
{
    public static KubeConfig getDefaultKubeConfig()
    {
        KubeConfig kubeConfig = new KubeConfig();
        kubeConfig.setMasterUrl("https://api.alpha.k8s.aort.theplatform.com");
        kubeConfig.setNameSpace("dfh");
        return kubeConfig;
    }

    public static PodConfig getDefaultPodConfig()
    {
        NfsDetails nfsDetails = new NfsDetails();
        nfsDetails.setNfsServerPath("/");
        nfsDetails.setNfsReadOnly(false);
        nfsDetails.setNfsMountPaths(new String[] { "/testFiles" });
        nfsDetails.setNfsServer("fs-21cc1888.efs.us-west-2.amazonaws.com");

        AliveCheckDetails aliveCheckDetails = new AliveCheckDetails();
        aliveCheckDetails.setAliveCheckHost("example.com");
        aliveCheckDetails.setAlivePort(14506);
        aliveCheckDetails.setAlivePath("/management/alive");
        aliveCheckDetails.setAliveCheckLinking(false);

        PodConfig podConfig = new PodConfig().applyDefaults();
        podConfig.setNfsDetails(nfsDetails);
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