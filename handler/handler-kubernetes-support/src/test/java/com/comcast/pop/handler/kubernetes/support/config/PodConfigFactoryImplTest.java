package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.comcast.pop.handler.kubernetes.support.config.PodConfigStations.*;
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
    public void testFactoryWithTaintedNodeConfig(final Boolean useTaintedNodesFlag, final String expectedSelector, final String expectedToleration)
    {
        doReturn(useTaintedNodesFlag).when(mockRetriever).getBoolean(useTaintedNodes.getFieldName(), false);
        doReturn(expectedSelector).when(mockRetriever).getField(useTaintedNodesSelector.getFieldName(), null);
        doReturn(expectedToleration).when(mockRetriever).getField(useTaintedNodesToleration.getFieldName(),null);

        configFactory = new PodConfigFactoryImpl(mockRetriever);
        PodConfig podConfig = configFactory.createPodConfig();

        Assert.assertNotNull(podConfig);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(podConfig.getUseTaintedNodes()).isEqualTo(useTaintedNodesFlag);
            softly.assertThat(podConfig.getTaintedNodeSelector()).isEqualTo(expectedSelector);
            softly.assertThat(podConfig.getTaintedNodeToleration()).isEqualTo(expectedToleration);
                });

    }
}
