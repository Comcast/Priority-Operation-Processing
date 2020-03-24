package com.theplatform.dfh.cp.handler.sample.test;

import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import javax.annotation.Resource;

@ContextConfiguration(locations = "classpath:spring-cp-handler-sample.xml")
public class SampleHandlerTestBase extends AbstractTestNGSpringContextTests
{
    protected JsonHelper jsonHelper = new JsonHelper();

    @Resource
    KubeConfig kubeConfig;

    @Resource
    PodConfig podConfig;
}
