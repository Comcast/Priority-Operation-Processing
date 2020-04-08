package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.fabric8.client.factory.OAuthCredentialCapture;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

public class KubeConfigFactoryImpl implements KubeConfigFactory
{
    private static Logger logger = LoggerFactory.getLogger(KubeConfigFactoryImpl.class);

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
        logger.info("Kubectl config values " + KubeConfigField.MASTER_URL.getFieldName() + ": " + propertiesRetriever.getField(KubeConfigField.MASTER_URL.getFieldName())
                    + ", " + KubeConfigField.NAMESPACE.getFieldName() + ": " + propertiesRetriever.getField(KubeConfigField.NAMESPACE.getFieldName())
                    + ", " + KubeConfigField.ZONE.getFieldName() + ": " + propertiesRetriever.getField(KubeConfigField.ZONE.getFieldName()));

        KubeConfig kubeConfig = new KubeConfig();
        if (propertiesRetriever.getField(KubeConfigField.MASTER_URL.getFieldName()) == null ||
                propertiesRetriever.getField(KubeConfigField.MASTER_URL.getFieldName()).length() == 0)
        {
            String serviceHost = System.getenv("KUBERNETES_SERVICE_HOST");
            String servicePort = System.getenv("KUBERNETES_SERVICE_PORT");
            String masterUrl = "https://" + serviceHost + ":" + servicePort;
            kubeConfig.setMasterUrl(masterUrl);
            logger.info("Master URL not defined, using environment variable: [" + masterUrl + "]");
        }
        else
        {
            kubeConfig.setMasterUrl(propertiesRetriever.getField(KubeConfigField.MASTER_URL.getFieldName()));
            logger.info("Master URL defined, using: [" + (propertiesRetriever.getField(KubeConfigField.MASTER_URL.getFieldName())) + "]");
        }
        kubeConfig.setNameSpace(propertiesRetriever.getField(KubeConfigField.NAMESPACE.getFieldName()));

        kubeConfig.setZone(propertiesRetriever.getField(KubeConfigField.ZONE.getFieldName()));

        //NOTE: auth loading from args/env vars is for local only
        if(!loadAuthFromArgs(kubeConfig)
            && loadAuthFromEnvironment)
            loadAuthFromEnvironment(kubeConfig);

        return kubeConfig;
    }

    private boolean loadAuthFromArgs(KubeConfig kubeConfig)
    {
        String oauthCertFilePath = launchDataWrapper.getArgumentRetriever().getField(KubeConfigArgument.OAUTH_CERT_FILE_PATH, null);
        String oauthTokenFilePath = launchDataWrapper.getArgumentRetriever().getField(KubeConfigArgument.OAUTH_TOKEN_FILE_PATH, null);

        if(oauthCertFilePath == null && oauthTokenFilePath == null)
        {
            return false;
        }
        else if(oauthCertFilePath == null)
        {
            logger.warn(KubeConfigArgument.OAUTH_CERT_FILE_PATH + " must be specified to use OAUTH file arguments.");
            return false;
        }
        else if(oauthTokenFilePath == null)
        {
            logger.warn(KubeConfigArgument.OAUTH_TOKEN_FILE_PATH + " must be specified to use OAUTH file arguments.");
            return false;
        }

        String oauthCert = readFileIntoString(oauthCertFilePath);
        if(StringUtils.isBlank(oauthCert))
        {
            logger.warn("Invalid cert from file: " + oauthCertFilePath + " (ignoring auth args)");
            return false;
        }
        String oauthToken = readFileIntoString(oauthTokenFilePath);
        if(StringUtils.isBlank(oauthCert))
        {
            logger.warn("Invalid token from file: " + oauthToken + " (ignoring auth args)");
            return false;
        }
        kubeConfig.setCaCertData(oauthCert);
        kubeConfig.setOauthToken(oauthToken);
        return true;
    }

    private String readFileIntoString(String filePath)
    {
        try
        {
            return new String(Files.readAllBytes(new File(filePath).toPath()));
        }
        catch(Exception e)
        {
            logger.error("Failed to read file: " + filePath, e);
        }
        return null;
    }

    private boolean loadAuthFromEnvironment(KubeConfig kubeConfig)
    {
        OAuthCredentialCapture oauthCredentialCapture = new OAuthCredentialCapture().init();
        if (oauthCredentialCapture.isOAuthAvailable())
        {
            kubeConfig.setCaCertData(oauthCredentialCapture.getOauthCert());
            kubeConfig.setOauthToken(oauthCredentialCapture.getOauthToken());
            return true;
        }
        return  false;
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
