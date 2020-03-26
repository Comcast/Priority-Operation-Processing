package com.theplatform.module.docker.elastic;

/**
 *
 */
public interface InstanceRegulatorClient
{
    public boolean startInstance(String nameSuffix);

    public int getCurrentInstanceCount();

    public void stopInstance(String nameSuffix);

    public boolean checkInstance(String nameSuffix);

    public void stopAll();

    void close();
}
