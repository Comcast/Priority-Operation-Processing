package com.theplatform.dfh.cp.modules.kube.fabric8.client.factory;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClientImpl;
import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.KubernetesClientFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.RetryableKubernetesClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.http.HttpClientUtilsEx;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.ConnectionTracker;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;

public class PodPushClientFactoryImpl extends PodPushClientFactory<CpuRequestModulator>
{

    public PodPushClient getClient(KubeConfig kubeConfig, ConnectionTracker connectionTracker)
    {
        OAuthCredentialCapture oauthCredentialCapture = new OAuthCredentialCapture().init();
        if (oauthCredentialCapture.isOAuthAvailable())
        {
            kubeConfig.setCaCertData(oauthCredentialCapture.getOauthCert());
            kubeConfig.setOauthToken(oauthCredentialCapture.getOauthToken());
        }

        Config config = Fabric8Helper.getFabric8Config(kubeConfig);
        DefaultKubernetesClient innerClient = new DefaultKubernetesClient(
            HttpClientUtilsEx.createHttpClient(config, connectionTracker),
            config);
        KubernetesClientFacade kubernetesClientFacade = new RetryableKubernetesClient(innerClient);
        PodPushClientImpl client = new PodPushClientImpl();
        client.setKubeConfig(kubeConfig);
        client.setKubernetesClient(kubernetesClientFacade);

        return client;
    }
}
