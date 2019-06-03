package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PodConfigFactoryImplTest
{
    final String TAINT_SELECTOR = "taintSelector";
    final String TAINT_TOLERATION = "taintToleration";

    private PodConfigFactoryImpl configFactory;
    private FieldRetriever mockRetriever;


    @BeforeMethod
    public void setup()
    {
        mockRetriever = mock(FieldRetriever.class);
    }

    @Test
    public void testFactoryWithoutRetriever()
    {
        configFactory = new PodConfigFactoryImpl();
        PodConfig podConfig = configFactory.createPodConfig();
        Assert.assertNotNull(podConfig);
    }

    @DataProvider
    public Object[][] taintedNodeSettingsProvider()
    {
        return new Object[][]
            {
                {false, null, null},
                {true, TAINT_SELECTOR, TAINT_TOLERATION}
            };
    }

    @Test(dataProvider = "taintedNodeSettingsProvider")
    public void testFactoryWithTaintedNodeConfig(final Boolean useTaintedNodes, final String expectedSelector, final String expectedToleration)
    {
        doReturn(useTaintedNodes).when(mockRetriever).getBoolean(PodConfigFactoryImpl.USE_TAINTED_NODES_PROPERTY, false);
        doReturn(expectedSelector).when(mockRetriever).getField(PodConfigFactoryImpl.TAINTED_NODES_SELECTOR_PROPERTY);
        doReturn(expectedToleration).when(mockRetriever).getField(PodConfigFactoryImpl.TAINTED_NODES_TOLERATION_PROPERTY);

        configFactory = new PodConfigFactoryImpl(mockRetriever);
        PodConfig podConfig = configFactory.createPodConfig();

        Assert.assertNotNull(podConfig);
        Assert.assertEquals(podConfig.getUseTaintedNodes(), useTaintedNodes);
        Assert.assertEquals(podConfig.getTaintedNodeSelector(), expectedSelector);
        Assert.assertEquals(podConfig.getTaintedNodeToleration(), expectedToleration);
    }
}
