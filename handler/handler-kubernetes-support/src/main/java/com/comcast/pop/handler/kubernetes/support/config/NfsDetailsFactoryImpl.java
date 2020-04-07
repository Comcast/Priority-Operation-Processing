package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.kube.client.config.NfsDetails;

public class NfsDetailsFactoryImpl implements NfsDetailsFactory
{
    protected LaunchDataWrapper launchDataWrapper;

    public NfsDetailsFactoryImpl(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    /**
     * Provides the NFS details based on the property prefix (extracted from the properties file)
     * @param propertyPrefix The prefix to use when looking up the nfs settings
     * @return a new NfsDetails
     */
    @Override
    public NfsDetails createNfsDetails(String propertyPrefix)
    {
        String server = getPropertyValue(propertyPrefix, NfsDetailsField.nfsServer);
        String serverPath = getPropertyValue(propertyPrefix, NfsDetailsField.nfsServerPath);
        String readOnly = getPropertyValue(propertyPrefix, NfsDetailsField.nfsReadOnly);
        String mountPaths = getPropertyValue(propertyPrefix, NfsDetailsField.nfsMountPaths);

        if(serverPath != null && mountPaths != null && server != null)
        {
            return new NfsDetails()
                .setNfsServer(server)
                .setNfsServerPath(serverPath)
                .setNfsReadOnly(Boolean.parseBoolean(readOnly))
                .setNfsMountPaths(mountPaths.split(","));
        }
        return null;
    }

    protected String getPropertyValue(String prefix, NfsDetailsField nfsDetailsField)
    {
        return launchDataWrapper.getPropertyRetriever().getField(prefix + nfsDetailsField.getFieldName());
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }
}
