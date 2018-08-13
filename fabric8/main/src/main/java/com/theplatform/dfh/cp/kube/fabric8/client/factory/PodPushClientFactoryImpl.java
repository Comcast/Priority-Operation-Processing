package com.theplatform.dfh.cp.kube.fabric8.client.factory;

import com.theplatform.dfh.cp.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.kube.fabric8.client.PodPushClientImpl;
import com.theplatform.dfh.cp.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.kube.client.config.KubeConfig;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import com.theplatform.dfh.cp.kube.fabric8.client.Fabric8Helper;

public class PodPushClientFactoryImpl extends PodPushClientFactory<CpuRequestModulator>
{

    public PodPushClient getClient(KubeConfig kubeConfig)
    {
        OAuthCredentialCapture oauthCredentialCapture = new OAuthCredentialCapture().init();
        if (oauthCredentialCapture.isOAuthAvailable())
        {
            kubeConfig.setCaCertData(oauthCredentialCapture.getOauthCert());
            kubeConfig.setOauthToken(oauthCredentialCapture.getOauthToken());
        }

        Config config = Fabric8Helper.getFabric8Config(kubeConfig);
        DefaultKubernetesClient innerClient = new DefaultKubernetesClient(config);
        PodPushClientImpl client = new PodPushClientImpl();
        client.setKubeConfig(kubeConfig);
        client.setFabric8Client(innerClient);

        return client;
    }
}
