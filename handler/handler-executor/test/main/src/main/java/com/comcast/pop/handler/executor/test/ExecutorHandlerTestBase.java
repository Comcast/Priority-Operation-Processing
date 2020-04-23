package com.comcast.pop.handler.executor.test;

import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import javax.annotation.Resource;
import java.io.IOException;

@ContextConfiguration(locations = "classpath:spring-cp-handler-executor.xml")
public class ExecutorHandlerTestBase extends AbstractTestNGSpringContextTests
{
    @Resource
    KubeConfig kubeConfig;

    @Resource
    PodConfig podConfig;

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }
}
