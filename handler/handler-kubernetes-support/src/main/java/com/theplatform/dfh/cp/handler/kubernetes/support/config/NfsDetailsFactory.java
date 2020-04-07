package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.modules.kube.client.config.NfsDetails;

public interface NfsDetailsFactory
{
    NfsDetails createNfsDetails(String propertyPrefix);
}
