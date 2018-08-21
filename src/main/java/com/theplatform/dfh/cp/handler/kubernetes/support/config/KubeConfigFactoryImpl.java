package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.OAuthCredentialCapture;

public class KubeConfigFactoryImpl implements KubeConfigFactory
{
    private LaunchDataWrapper launchDataWrapper;
    private boolean loadAuthFromEnvironment;

    public KubeConfigFactoryImpl(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    @Override
    public KubeConfig createKubeConfig()
    {
        FieldRetriever propertiesRetriever = launchDataWrapper.getPropertyRetriever();

        KubeConfig kubeConfig = new KubeConfig();
        kubeConfig.setMasterUrl(propertiesRetriever.getField(KubeConfigField.MASTER_URL));
        kubeConfig.setNameSpace(propertiesRetriever.getField(KubeConfigField.NAMESPACE));

        if(loadAuthFromEnvironment)
        {
            OAuthCredentialCapture oauthCredentialCapture = new OAuthCredentialCapture().init();
            if (oauthCredentialCapture.isOAuthAvailable())
            {
                kubeConfig.setCaCertData(oauthCredentialCapture.getOauthCert());
                kubeConfig.setOauthToken(oauthCredentialCapture.getOauthToken());
            }
        }

        return kubeConfig;
    }

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public KubeConfigFactoryImpl setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
        return this;
    }

    public boolean isLoadAuthFromEnvironment()
    {
        return loadAuthFromEnvironment;
    }

    public KubeConfigFactoryImpl setLoadAuthFromEnvironment(boolean loadAuthFromEnvironment)
    {
        this.loadAuthFromEnvironment = loadAuthFromEnvironment;
        return this;
    }
}
