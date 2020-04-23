package com.comcast.pop.modules.kube.fabric8.client.factory;

import com.comcast.pop.modules.kube.fabric8.client.Fabric8Helper;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClientImpl;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.fabric8.client.client.KubernetesHttpClientsFactory;
import io.fabric8.kubernetes.client.Config;

public class PodPushClientFactoryImpl extends PodPushClientFactory<CpuRequestModulator>
{
    private KubernetesHttpClientsFactory httpClientsFactory = new KubernetesHttpClientsFactory();

    public PodPushClient getClient(KubeConfig kubeConfig)
    {
        OAuthCredentialCapture oauthCredentialCapture = new OAuthCredentialCapture().init();
        if (oauthCredentialCapture.isOAuthAvailable())
        {
            kubeConfig.setCaCertData(oauthCredentialCapture.getOauthCert());
            kubeConfig.setOauthToken(oauthCredentialCapture.getOauthToken());
        }

        Config config = Fabric8Helper.getFabric8Config(kubeConfig);

        PodPushClientImpl client = new PodPushClientImpl();
        client.setKubeConfig(kubeConfig);
        client.setKubernetesHttpClients(httpClientsFactory.createClients(config));

        return client;
    }

    public PodPushClientFactoryImpl setHttpClientsFactory(KubernetesHttpClientsFactory httpClientsFactory)
    {
        this.httpClientsFactory = httpClientsFactory;
        return this;
    }
}
