package com.comcast.pop.handler.kubernetes.support.config;

import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PodConfigRegistry based PodConfigFactory
 */
public class RegistryPodConfigFactory implements PodConfigFactory
{
    private static Logger logger = LoggerFactory.getLogger(RegistryPodConfigFactory.class);
    private PodConfigRegistryClient registryClient;

    public RegistryPodConfigFactory(PodConfigRegistryClient registryClient)
    {
        this.registryClient = registryClient;
    }

    @Override
    public PodConfig createPodConfig()
    {
        return new PodConfig().applyDefaults();
    }

    /**
     * Creates a PodConfig based on the specified template. If the template does not exist null is returned
     * @param templateName The name of the template to generate a PodConfig from
     * @return PodConfig or null if template not found
     */
    @Override
    public PodConfig createPodConfig(String templateName)
    {
        try
        {
            return registryClient.getPodConfig(templateName);
        }
        catch(PodConfigRegistryClientException e)
        {
            logger.error("Unable to create PodConfig from templateName: " + templateName, e);
            return null;
        }
    }

    public PodConfigRegistryClient getRegistryClient()
    {
        return registryClient;
    }

    public void setRegistryClient(PodConfigRegistryClient registryClient)
    {
        this.registryClient = registryClient;
    }
}
