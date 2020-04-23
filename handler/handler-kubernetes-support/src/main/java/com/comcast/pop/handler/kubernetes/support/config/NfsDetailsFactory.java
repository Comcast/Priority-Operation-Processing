package com.comcast.pop.handler.kubernetes.support.config;

import com.comcast.pop.modules.kube.client.config.NfsDetails;

public interface NfsDetailsFactory
{
    NfsDetails createNfsDetails(String propertyPrefix);
}
