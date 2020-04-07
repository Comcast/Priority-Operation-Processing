package com.comcast.pop.handler.puller.impl.limit;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.argument.ArgumentRetriever;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigArgument;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigField;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * We could build a facade for unit testing the KubernetesInsightExecutionResourceChecker but it's not really complex...
 */
public class KubernetesInsightExecutionResourceCheckerTest
{
    @Test(enabled = false)
    public void liveTestOfPodQuery()
    {
        LaunchDataWrapper mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        PropertyRetriever mockPropertiesRetriever = mock(PropertyRetriever.class);
        ArgumentRetriever mockArgumentRetriever = mock(ArgumentRetriever.class);
        doReturn("https://api.bravo.k8s.aort.theplatform.com").when(mockPropertiesRetriever).getField(KubeConfigField.MASTER_URL.getFieldName());
        doReturn("fission").when(mockPropertiesRetriever).getField(KubeConfigField.NAMESPACE.getFieldName());
        doReturn("~/pop-service.ca.cert").when(mockArgumentRetriever).getField(KubeConfigArgument.OAUTH_CERT_FILE_PATH, null);
        doReturn("~/pop-service.sa.token").when(mockArgumentRetriever).getField(KubeConfigArgument.OAUTH_TOKEN_FILE_PATH, null);
        doReturn(mockPropertiesRetriever).when(mockLaunchDataWrapper).getPropertyRetriever();
        doReturn(mockArgumentRetriever).when(mockLaunchDataWrapper).getArgumentRetriever();

        KubeConfig kubeConfig = new KubeConfigFactoryImpl(mockLaunchDataWrapper).createKubeConfig();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig));
        List<Pod> executors = kubernetesClient.pods().inNamespace(kubernetesClient.getNamespace())
            .list().getItems();
        Set<String> podStatusSet = new HashSet<>();
        podStatusSet.addAll(Arrays.asList(PodPhase.PENDING.getLabel(), PodPhase.RUNNING.getLabel(), PodPhase.UNKNOWN.getLabel()));
        List<Pod> pods = executors.stream().filter(pod ->
            pod.getStatus() != null && podStatusSet.contains(pod.getStatus().getPhase()))
            .collect(Collectors.toList());
        System.out.println(pods.size());
    }
}
