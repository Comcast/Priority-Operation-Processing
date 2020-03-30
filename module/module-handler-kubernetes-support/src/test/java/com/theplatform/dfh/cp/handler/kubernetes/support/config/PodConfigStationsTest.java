package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PodConfigStationsTest
{
    private static final Object[] defaults = {null, null, null, false, false, false, null, null, 300000L, 120000L, 0, 3000, null };
    private static final Object[] testValues = {"test end of log id", "test name prefix", "test docker image name", true, true, true, "test tainted node selector", "test tainted node toleration", 99999999L, 8888888L, 7, 6666, "service-account" };
    private PodConfigTestHelper helper = new PodConfigTestHelper();

    /**
     *  It is expected that this test will need to be updated if elements of the PodConfigStations are added or removed or modified;
     */
    @Test
    public void testPodConfigStationDefaultsWithNoPropertyRetriever()
    {
        assertThat(defaults).hasSameSizeAs(PodConfigStations.values());
        Optional<FieldRetriever> fieldRetriever = Optional.empty();
        PodConfig podConfig = new PodConfig();
        Arrays.stream(PodConfigStations.values()).forEach(station -> station.setPodConfig(podConfig, fieldRetriever));
        helper.validatePodConfig(podConfig, defaults);
    }

    /**
     *  It is expected that this test will need to be updated if elements of the PodConfigStations are added or removed or modified;
     */
    @Test
    public void testPodConfigStationDefaultsWithNothingConfigured()
    {
        assertThat(defaults).hasSameSizeAs(PodConfigStations.values());
        FieldRetriever propertyRetriever = new PropertyRetriever(null);
        Optional<FieldRetriever> fieldRetriever = Optional.ofNullable(propertyRetriever);
        PodConfig podConfig = new PodConfig();
        Arrays.stream(PodConfigStations.values()).forEach(station -> station.setPodConfig(podConfig, fieldRetriever));
        helper.validatePodConfig(podConfig, defaults);
    }

    /**
     *  It is expected that this test will need to be updated if elements of the PodConfigStations are added or removed or modified;
     */
    @Test
    public void testPodConfigStationConfigured()
    {
        assertThat(testValues).hasSameSizeAs(PodConfigStations.values());
        FieldRetriever mockFieldRetriever = helper.makeMockFieldRetriver(testValues);
        Optional<FieldRetriever> fieldRetriever = Optional.ofNullable(mockFieldRetriever);
        PodConfig podConfig = new PodConfig();
        Arrays.stream(PodConfigStations.values()).forEach(station -> station.setPodConfig(podConfig, fieldRetriever));
        helper.validatePodConfig(podConfig, testValues);
    }
}
class PodConfigTestHelper
        {
            protected FieldRetriever makeMockFieldRetriver(Object[] testValues)
            {
                assertThat(testValues).hasSameSizeAs(PodConfigStations.values());
                FieldRetriever mockRetriever = mock(FieldRetriever.class);
                doReturn(testValues[0]).when(mockRetriever).getField(PodConfigStations.eolIdentifier.getFieldName(), null);
                doReturn(testValues[1]).when(mockRetriever).getField(PodConfigStations.namePrefix.getFieldName(), null);
                doReturn(testValues[2]).when(mockRetriever).getField(PodConfigStations.imageName.getFieldName(), null);
                doReturn(testValues[3]).when(mockRetriever).getBoolean(PodConfigStations.dockerImagePullAlways.getFieldName(), false);
                doReturn(testValues[4]).when(mockRetriever).getBoolean(PodConfigStations.reapCompletedPods.getFieldName(), false);
                doReturn(testValues[5]).when(mockRetriever).getBoolean(PodConfigStations.useTaintedNodes.getFieldName(), false);
                doReturn(testValues[6]).when(mockRetriever).getField(PodConfigStations.useTaintedNodesSelector.getFieldName(), null);
                doReturn(testValues[7]).when(mockRetriever).getField(PodConfigStations.useTaintedNodesToleration.getFieldName(), null);
                doReturn(testValues[8]).when(mockRetriever).getLong(PodConfigStations.podTimeoutScheduledMS.getFieldName(), 300000L);
                doReturn(testValues[9]).when(mockRetriever).getLong(PodConfigStations.podTimeoutStdOut.getFieldName(), 120000L);
                doReturn(testValues[10]).when(mockRetriever).getInt(PodConfigStations.retryCount.getFieldName(), 0);
                doReturn(testValues[11]).when(mockRetriever).getInt(PodConfigStations.retryDelay.getFieldName(), 3000);
                doReturn(testValues[12]).when(mockRetriever).getField(PodConfigStations.serviceAccount.getFieldName(), null);

                return mockRetriever;
            }

            protected void validatePodConfig(PodConfig podConfig, Object[] expectedValues)
            {
                assertThat(podConfig).isNotNull();
                assertThat(expectedValues).isNotEmpty();
                assertThat(expectedValues).hasSameSizeAs(PodConfigStations.values());
                SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(podConfig.getEndOfLogIdentifier()).isEqualTo(expectedValues[0]);
                    softly.assertThat(podConfig.getNamePrefix()).isEqualTo(expectedValues[1]);
                    softly.assertThat(podConfig.getImageName()).isEqualTo(expectedValues[2]);
                    softly.assertThat(podConfig.getPullAlways()).isEqualTo(expectedValues[3]);
                    softly.assertThat(podConfig.getReapCompletedPods()).isEqualTo(expectedValues[4]);
                    softly.assertThat(podConfig.getUseTaintedNodes()).isEqualTo(expectedValues[5]);
                    softly.assertThat(podConfig.getTaintedNodeSelector()).isEqualTo(expectedValues[6]);
                    softly.assertThat(podConfig.getTaintedNodeToleration()).isEqualTo(expectedValues[7]);
                    softly.assertThat(podConfig.getPodScheduledTimeoutMs()).isEqualTo(expectedValues[8]);
                    softly.assertThat(podConfig.getPodStdoutTimeout()).isEqualTo(expectedValues[9]);
                    softly.assertThat(podConfig.getPodRetryCount()).isEqualTo(expectedValues[10]);
                    softly.assertThat(podConfig.getRetryDelayMilliSecs()).isEqualTo(expectedValues[11]);
                    softly.assertThat(podConfig.getServiceAccountName()).isEqualTo(expectedValues[12]);
                });
            }
        }