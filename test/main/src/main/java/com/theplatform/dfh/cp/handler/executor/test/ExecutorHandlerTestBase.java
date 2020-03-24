package com.theplatform.dfh.cp.handler.executor.test;

import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import javax.annotation.Resource;
import java.io.IOException;

@ContextConfiguration(locations = "classpath:spring-cp-handler-executor.xml")
public class ExecutorHandlerTestBase extends AbstractTestNGSpringContextTests
{
    protected JsonHelper jsonHelper = new JsonHelper();

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
